package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.braintreepayments.api.models.CardNonce
import com.braintreepayments.api.models.PaymentMethodNonce
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.activity.ObtainGrailActivity
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.screens.fragment.dialog.InputPasswordDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_confirm_transfer.*
import kotlinx.android.synthetic.main.fragment_create_transfer.ivLimited
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailBrand
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailName
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailReleaseDate
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailSize
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ConfirmTransferFragment : BaseFragment(), View.OnClickListener {

    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverEosName: String? = null
    private var receiverName: String? = null
    private var receiverAvatar: String? = null
    private var currentUser: User? = null
    private var password: String? = null
    private var receiverId: Int? = null
    private var paymentMethodNonce: PaymentMethodNonce? = null
    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    override fun getLayoutId(): Int {
        return R.layout.fragment_confirm_transfer
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_transfer
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        service = RetrofitClientInstance().getRetrofitInstance()!!

        sharedPref = context?.let { SharedPref(it) }!!
        currentUser = sharedPref.getUser(Constant.LOGIN_USER)?.user

        sneaker = activity?.intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel
        receiverName = arguments?.getString(Constant.EXTRA_RECEIVER_NAME)
        receiverEosName = arguments?.getString(Constant.EXTRA_RECEIVER_EOS_NAME)
        receiverId = arguments?.getInt(Constant.EXTRA_RECEIVER_ID, 0)
        receiverAvatar = arguments?.getString(Constant.EXTRA_RECEIVER_AVATAR)
        paymentMethodNonce = arguments?.getParcelable(Constant.EXTRA_PAYMENT_NONCE)


        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSneakerInfo()
        loadReceiverInfo()
        loadCardInfo()

        btnConfirm.setOnClickListener(this)
    }

    private fun loadSneakerInfo() {
        tvGrailName.text = sneaker?.model
        tvGrailBrand.text = sneaker?.brand
        tvGrailSize.text = sneaker?.size.toString()
        tvGrailReleaseDate.text = sneaker?.releaseDate

        if (sneaker?.limitedEdition!!) {
            ivLimited.visibility = View.VISIBLE
        }
    }

    private fun loadReceiverInfo() {
        tvReceiverName.text = receiverName
        tvReceiverPhone.text = receiverEosName

        if (!receiverAvatar.isNullOrEmpty()) {
            Picasso.get().load(receiverAvatar).into(ivReceiverAvatar)
        }
    }

    private fun loadCardInfo() {
        tvCardId.text = getString(R.string.format_credit_card, (paymentMethodNonce as CardNonce).lastFour)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnConfirm -> {
                InputPasswordDialogFragment.show(
                    this@ConfirmTransferFragment, fragmentManager!!,
                    resources.getString(R.string.dialog_title_confirm_passcode),
                    resources.getString(R.string.dialog_message_confirm_passcode)
                )
            }
        }
    }

    private fun createTransaction() {
        CommonUtils.toggleLoading(fragmentView, true)
        val jsonData = ContractRequest.transferSneakerJson(
            sneaker?.id!!,
            receiverId!!
        )

        ContractRequest.callEosApi(currentUser?.eosName!!,
            ContractRequest.METHOD_TRANSFER,
            jsonData,
            getString(R.string.format_eascrypt_password, password),
            currentUser?.encryptedPrivateKey,
            null,
            object : ContractRequest.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        Toast.makeText(context, "Transaction id: $result", Toast.LENGTH_LONG).show()
                        chargeTransactionFee()
                    } else {
                        CommonUtils.toggleLoading(fragmentView, false)
                        if (e.message == "pad block corrupted") {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(getString(R.string.msg_wrong_password))
                            alertDialogFragment.show(
                                fragmentManager!!,
                                AlertDialogFragment::class.java.simpleName
                            )
                        } else {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(e.message)
                            alertDialogFragment.show(
                                fragmentManager!!,
                                AlertDialogFragment::class.java.simpleName
                            )
                        }
                        password = ""
                    }
                }

            })
    }

    private fun chargeTransactionFee() {
        val params = HashMap<String, Any>()
        params[Constant.API_FIELD_PAYMENT_NONCE] = paymentMethodNonce?.nonce!!

        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val call = service.create(MainApi::class.java)
            .createTransaction(
                accessToken,
                sharedPref.getUser(Constant.LOGIN_USER)?.user?.id!!,
                params
            )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    fragmentManager!!,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    pushNotification()
                } else {
                    CommonUtils.toggleLoading(fragmentView, false)
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(response.message())
                    alertDialogFragment.show(
                        fragmentManager!!,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

        })
    }

    private fun pushNotification() {
        val params = HashMap<String, Any>()
        params[Constant.API_FIELD_SEANER_ID] = sneaker?.id!!
        params[Constant.API_FIELD_NEW_OWNER_ID] = receiverId!!

        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val call = service.create(MainApi::class.java)
            .pushTransferNotification(
                accessToken,
                params
            )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.localizedMessage)
                alertDialogFragment.show(
                    fragmentManager!!,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                CommonUtils.toggleLoading(fragmentView, false)
                if (response.isSuccessful) {
                    ObtainGrailActivity.start(activity!!, sneaker!!)
                    EventBus.getDefault().post(ReloadCollectionEvent())
                    activity?.finish()
                } else {
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(response.message())
                    alertDialogFragment.show(
                        fragmentManager!!,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data?.extras?.containsKey(Constant.EXTRA_USER_PASSWORD)!!) {
                password = data.extras?.getString(Constant.EXTRA_USER_PASSWORD)
                createTransaction()
            }
        }
    }
}