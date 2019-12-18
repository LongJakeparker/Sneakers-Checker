package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.models.CardNonce
import com.braintreepayments.api.models.PaymentMethodNonce
import com.braintreepayments.cardform.view.CardForm
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_create_transfer.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class CreateTransferFragment : Fragment(), View.OnClickListener {
    val DROP_IN_REQUEST = 1005
    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverName: String = ""
    private var receiverId: Int? = null
    private var receiverAvatar: String? = null
    private var paymentMethodNonce: PaymentMethodNonce? = null
    private lateinit var service: Retrofit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_transfer, container, false)

        sneaker = activity?.intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel

        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSneakerInfo()

        btnClose.setOnClickListener(this)
        ivClearPhone.setOnClickListener(this)
        btnContinue.setOnClickListener(this)
        root.setOnClickListener(this)
        llAddPayment.setOnClickListener(this)
        ivDeletePayment.setOnClickListener(this)
        etReceiverPhone.addTextChangedListener(textWatcher)
    }

    private fun submitPaymentMethod(clienToken: String) {
        val dropInRequest = DropInRequest()
            .cardholderNameStatus(CardForm.FIELD_REQUIRED)
            .vaultCard(true)
            .vaultManager(true)
            .clientToken(clienToken)
        startActivityForResult(dropInRequest.getIntent(context), DROP_IN_REQUEST)
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

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                ivClearPhone.visibility = View.VISIBLE
                ivClearPhone.isClickable = true
            } else {
                ivClearPhone.visibility = View.INVISIBLE
                ivClearPhone.isClickable = false
            }
            btnContinue.isEnabled = validateData()
        }
    }

    private fun validateData(): Boolean {
        return (etReceiverPhone.text.isNotEmpty() && paymentMethodNonce != null)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnClose -> activity?.finish()

            ivClearPhone -> {
                etReceiverPhone.text.clear()
            }

            root -> CommonUtils.hideKeyboard(activity)

            btnContinue -> checkPhoneNumber()

            llAddPayment -> submitPaymentMethod(CommonUtils.getBrainTreeToken(context!!))

            ivDeletePayment -> {
                tvCardId.text = getString(R.string.text_add_payment)
                ivAddPayment.visibility = View.VISIBLE
                ivDeletePayment.visibility = View.GONE
                paymentMethodNonce = null
            }
        }
    }

    private fun checkPhoneNumber() {
        CommonUtils.toggleLoading(fragmentView, true)
        val call = service.create(MainApi::class.java)
            .getUserNameByPhone("+${pickerCountryCode.selectedCountryCode + etReceiverPhone.text.toString().trim()}")
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
                CommonUtils.toggleLoading(fragmentView, false)
                when {
                    response.isSuccessful -> {
                        try {
                            val jsonObject = JSONObject(response.body()?.string())
                            receiverName = jsonObject.getString("username")
                            receiverId = jsonObject.getInt("id")
                            receiverAvatar = jsonObject.getString("avatar")
                            confirmTransaction()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    response.code() == 400 -> {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance(getString(R.string.msg_eos_name_not_valid))
                        alertDialogFragment.show(
                            fragmentManager!!,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }

                    else -> {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance("Response Code ${response.code()}: ${response.message()}")
                        alertDialogFragment.show(
                            fragmentManager!!,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }
                }
            }

        })
    }

    private fun confirmTransaction() {
        val confirmTransferFragment = ConfirmTransferFragment()
        val bundle = Bundle()
        bundle.putSerializable(Constant.EXTRA_SNEAKER, sneaker)
        bundle.putString(Constant.EXTRA_RECEIVER_NAME, receiverName)
        bundle.putInt(Constant.EXTRA_RECEIVER_ID, receiverId!!)
        bundle.putString(Constant.EXTRA_RECEIVER_AVATAR, receiverAvatar)
        bundle.putString(
            Constant.EXTRA_RECEIVER_EOS_NAME,
            "+${pickerCountryCode.selectedCountryCode + etReceiverPhone.text.toString().trim()}"
        )
        bundle.putParcelable(Constant.EXTRA_PAYMENT_NONCE, paymentMethodNonce)
        confirmTransferFragment.arguments = bundle
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fragment_enter_from_right,
            R.anim.fragment_exit_to_left,
            R.anim.fragment_enter_from_left,
            R.anim.fragment_exit_to_right
        )
            .replace(R.id.fl_transfer_content, confirmTransferFragment)
            .addToBackStack(null).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                paymentMethodNonce = result.paymentMethodNonce
                val paymentNonce = result.paymentMethodNonce?.nonce!!
                Log.e("Payment Nonce: ", paymentNonce)

                tvCardId.text = getString(R.string.format_credit_card, (result.paymentMethodNonce as CardNonce).lastFour)
                ivAddPayment.visibility = View.GONE
                ivDeletePayment.visibility = View.VISIBLE
                btnContinue.isEnabled = validateData()
            } else if (resultCode == Activity.RESULT_CANCELED) { // canceled
            } else { // an error occurred, checked the returned exception
                val exception =
                    data!!.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception

                val alertDialogFragment =
                    AlertDialogFragment.newInstance(exception.localizedMessage)
                alertDialogFragment.show(
                    fragmentManager!!,
                    AlertDialogFragment::class.java.simpleName
                )
            }
        }
    }
}