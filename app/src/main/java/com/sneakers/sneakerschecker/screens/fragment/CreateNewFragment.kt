package com.sneakers.sneakerschecker.screens.fragment

import android.content.Intent
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
            btnRegister -> createNewAccount()

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

    private fun createNewAccount() {
        try {
            val generatePrivateKey = EosPrivateKey()
            val publicKey = generatePrivateKey.publicKey.toString()
            val encryptedPrivateKey = AESCrypt.encrypt(
                generatePrivateKey.toWif(),
                getString(R.string.format_eascrypt_password, etUserPassword.text.toString())
            )

            userRegister(publicKey, encryptedPrivateKey)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun userRegister(publicKey: String, encryptedPrivateKey: String) {
        CommonUtils.hideKeyboard(activity)
        var data = HashMap<String, String>()
        data[Constant.API_FIELD_USER_EMAIL] =
            "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}"
        data[Constant.API_FIELD_USER_PASSWORD] = etUserPassword.text.toString()
        data[Constant.API_FIELD_USER_ADDRESS] = "New York"
        data[Constant.API_FIELD_PUBLIC_KEY] = publicKey
        data[Constant.API_FIELD_ENCRYPTED_PRIVATE_KEY] = encryptedPrivateKey
        data[Constant.API_FIELD_USER_ROLE] = Constant.USER_ROLE_COLLECTOR
        data[Constant.API_FIELD_USER_NAME] = "Test Mobile"
        data[Constant.API_FIELD_EOS_NAME] = CommonUtils.generateEOSAccountName()
//        data[Constant.API_FIELD_REGISTRATION_TOKEN] = sharedPref.getString(Constant.FCM_TOKEN)

        CommonUtils.toggleLoading(fragmentView, true)

        /*Create handle for the RetrofitInstance interface*/
        val call = service.create(AuthenticationApi::class.java).signUpApi(data)
        call.enqueue(object : Callback<SignUp> {

            override fun onResponse(call: Call<SignUp>, response: Response<SignUp>) {
                when {
                    response.code() == 201 ->
                        requestLogIn()
                    response.code() == 400 -> {
                        CommonUtils.toggleLoading(fragmentView, false)
                        visibleWarning("Phone has been used")
                    }
                    else -> {
                        CommonUtils.toggleLoading(fragmentView, false)
                        Toast.makeText(
                            context,
                            "Response Code ${response.code()}: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignUp>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(
                    context,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun requestLogIn() {

        val authToken =
            okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                etUserPassword.text.toString().trim()
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}", // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        activity!!, // Activity (for callback binding)
                        callbacks) // OnVerificationStateChangedCallbacks

                } else {
                    CommonUtils.toggleLoading(fragmentView, false)
                    Toast.makeText(context, response.errorBody()!!.string(), Toast.LENGTH_SHORT)
                        .show()
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT)
                    .show()
            }

        })
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

            signInWithPhoneAuthCredential(credential)
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
            VerifyPhoneActivity.start(activity!!, verificationId, token)
            // ...
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        fireAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    FinishVerifyActivity.start(activity!!)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "Invalid verify code", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
