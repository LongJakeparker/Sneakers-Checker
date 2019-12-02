package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
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
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.eosCommander.crypto.ec.EosPrivateKey
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.activity.FinishVerifyActivity
import com.sneakers.sneakerschecker.screens.activity.VerifyPhoneActivity
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_create_new.*
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
class CreateNewFragment : Fragment(), View.OnClickListener {
    private val TAG = "PhoneAuth"

    private var fragmentView: View? = null

    private lateinit var service: Retrofit
    private lateinit var sharedPref: SharedPref

    private var isShowingPassword: Boolean = false

    private lateinit var fireAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_new, container, false)

        fireAuth = FirebaseAuth.getInstance()

        sharedPref = context?.let { SharedPref(it) }!!

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPhone.addTextChangedListener(textWatcher)
        etUserPassword.addTextChangedListener(textWatcher)
        btnRegister.setOnClickListener(this)
        ibBack.setOnClickListener(this)
        root.setOnClickListener(this)
        btnShowPassword.setOnClickListener(this)
    }


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnRegister.isEnabled = validateData()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnRegister -> {
                CommonUtils.toggleLoading(fragmentView, true)
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}", // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    activity!!, // Activity (for callback binding)
                    callbacks
                ) // OnVerificationStateChangedCallbacks
            }

            ibBack -> activity?.onBackPressed()

            btnShowPassword -> showPassword()

            root -> CommonUtils.hideKeyboard(activity)
        }
    }

    private fun showPassword() {
        val cursorStart = etUserPassword.selectionStart
        val cursorEnd = etUserPassword.selectionEnd
        if (!isShowingPassword) {
            etUserPassword.transformationMethod = null
            isShowingPassword = true
            etUserPassword.setSelection(cursorStart, cursorEnd)
            btnShowPassword.setImageResource(R.drawable.ic_hide_password)
        } else {
            etUserPassword.transformationMethod = PasswordTransformationMethod()
            isShowingPassword = false
            etUserPassword.setSelection(cursorStart, cursorEnd)
            btnShowPassword.setImageResource(R.drawable.ic_show_password)
        }
    }

    fun validateData(): Boolean {
        return !(etUserPhone.text.trim().length < 9 || etUserPassword.text.trim().length < 6)
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
            VerifyPhoneActivity.start(activity!!,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                etUserPassword.text.toString().trim(),
                credential)
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
            VerifyPhoneActivity.start(activity!!,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                etUserPassword.text.toString().trim(), verificationId, token)
            // ...
        }
    }
}
