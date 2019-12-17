package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import android.content.Intent.getIntent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.models.PaymentMethodNonce
import android.content.Intent.getIntent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.cardform.view.CardForm


class CreateTransferFragment : Fragment(), View.OnClickListener {
    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverName: String = ""
    private var receiverId: Int? = null
    private var receiverAvatar: String? = null
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

        getBrainTreeClientToken()

        loadSneakerInfo()

        btnClose.setOnClickListener(this)
        ivClearPhone.setOnClickListener(this)
        btnContinue.setOnClickListener(this)
        root.setOnClickListener(this)
        etReceiverPhone.addTextChangedListener(textWatcher)
    }

    private fun getBrainTreeClientToken() {
        val call = service.create(MainApi::class.java)
            .getBrainTreeClientToken(CommonUtils.getCurrentUser(context!!)?.id!!)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    fragmentManager!!,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                submitPaymentMethod()
            }

        })
    }

    private fun submitPaymentMethod() {
        val dropInRequest = DropInRequest()
            .cardholderNameStatus(CardForm.FIELD_REQUIRED)
            .vaultManager(true)
            .clientToken(mClientToken)
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
        return (etReceiverPhone.text.isNotEmpty())
    }

    override fun onClick(v: View?) {
        when (v) {
            btnClose -> activity?.finish()

            ivClearPhone -> {
                etReceiverPhone.text.clear()
            }

            root -> CommonUtils.hideKeyboard(activity)

            btnContinue -> checkPhoneNumber()
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
}