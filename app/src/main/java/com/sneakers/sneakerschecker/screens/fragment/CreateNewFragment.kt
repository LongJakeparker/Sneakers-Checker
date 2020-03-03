package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.screens.activity.VerifyPhoneActivity
import com.sneakers.sneakerschecker.screens.fragment.dialog.InputPasswordDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_create_new.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/**
 * A simple [Fragment] subclass.
 *
 */
class CreateNewFragment : BaseFragment(), View.OnClickListener {

    private val TAG = "PhoneAuth"

    private var fragmentView: View? = null
    private var password: String = ""
    private var isConfirmPassword: Boolean = false

    private lateinit var service: Retrofit
    private lateinit var sharedPref: SharedPref

    private lateinit var fireAuth: FirebaseAuth

    override fun getLayoutId(): Int {
        return R.layout.fragment_create_new
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_create_account
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = super.onCreateView(inflater, container, savedInstanceState)

        fireAuth = FirebaseAuth.getInstance()

        sharedPref = context?.let { SharedPref(it) }!!

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPhone.addTextChangedListener(textWatcher)
        btnNext.setOnClickListener(this)
        root.setOnClickListener(this)
    }


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnNext.isEnabled = validateData()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnNext -> {
                CommonUtils.toggleLoading(fragmentView, true)
                checkDuplicatePhone()
            }

            root -> CommonUtils.hideKeyboard(activity)
        }
    }

    private fun checkDuplicatePhone() {
        val call = service.create(MainApi::class.java).checkDuplicatePhone(
            "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}"
        )
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                CommonUtils.toggleLoading(fragmentView, false)
                when {
                    response.isSuccessful -> {
                        try {
                            val jsonObject = JSONObject(response.body()?.string())
                            val isExisting = jsonObject.getBoolean("isExisting")
                            if (!isExisting) {
                                InputPasswordDialogFragment.show(
                                    this@CreateNewFragment,
                                    fragmentManager!!,
                                    getString(R.string.dialog_title_create_passcode),
                                    getString(R.string.dialog_message_create_passcode)
                                )
                            } else {
                                tvWarning.text = getString(R.string.msg_phone_existing)
                                tvWarning.visibility = VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    else -> {
                        Toast.makeText(
                            context,
                            "Response Code ${response.code()}: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(
                    context,
                    t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun validateData(): Boolean {
        return etUserPhone.text.isNotEmpty()
    }

    fun visibleWarning(message: String) {
        tvWarning.text = "Oops!\n$message"
        tvWarning.visibility = VISIBLE
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")

            CommonUtils.toggleLoading(fragmentView, false)
            VerifyPhoneActivity.start(
                activity!!,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                password,
                credential
            )
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
            CommonUtils.toggleLoading(fragmentView, false)
            visibleWarning(e.message!!)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            CommonUtils.toggleLoading(fragmentView, false)
            VerifyPhoneActivity.start(
                activity!!,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                password, verificationId, token
            )
            // ...
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (!isConfirmPassword) {
                password = data?.extras?.getString(Constant.EXTRA_USER_PASSWORD, "")!!
                InputPasswordDialogFragment.show(
                    this@CreateNewFragment,
                    fragmentManager!!,
                    getString(R.string.dialog_title_re_enter_passcode),
                    getString(R.string.dialog_message_create_passcode)
                )
                isConfirmPassword = true
            } else {
                val confirmPassword = data?.extras?.getString(Constant.EXTRA_USER_PASSWORD, "")!!
                if  (confirmPassword != password) {
                    InputPasswordDialogFragment.show(
                        this@CreateNewFragment,
                        fragmentManager!!,
                        getString(R.string.dialog_title_re_enter_passcode),
                        getString(R.string.dialog_message_re_enter_wrong_passcode)
                    )
                } else {
                    CommonUtils.toggleLoading(fragmentView, true)
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}", // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        activity!!, // Activity (for callback binding)
                        callbacks
                    ) // OnVerificationStateChangedCallbacks
                }
            }
        }
    }
}
