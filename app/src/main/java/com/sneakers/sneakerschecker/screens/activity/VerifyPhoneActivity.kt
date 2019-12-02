package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_verify_phone.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit




class VerifyPhoneActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var listTvOTPCode: Array<TextView>
    private lateinit var sharedPref: SharedPref
    private var countDown: CountDownTimer? = null
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var phoneNumber: String? = null
    private var password: String? = null
    private var authCredential: PhoneAuthCredential? = null

    private lateinit var fireAuth: FirebaseAuth
    private lateinit var service: Retrofit

    companion object {
        fun start(
            activity: Activity,
            phoneNumber: String,
            password: String,
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            val intent = Intent(activity, VerifyPhoneActivity::class.java)
            intent.putExtra(Constant.EXTRA_USER_PHONE, phoneNumber)
            intent.putExtra(Constant.EXTRA_USER_PASSWORD, password)
            intent.putExtra(Constant.EXTRA_VERIFICATION_ID, verificationId)
            intent.putExtra(Constant.EXTRA_RESEND_TOKEN, token)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }

        fun start(
            activity: Activity,
            phoneNumber: String,
            password: String,
            credential: PhoneAuthCredential
        ) {
            val intent = Intent(activity, VerifyPhoneActivity::class.java)
            intent.putExtra(Constant.EXTRA_USER_PHONE, phoneNumber)
            intent.putExtra(Constant.EXTRA_USER_PASSWORD, password)
            intent.putExtra(Constant.EXTRA_USER_PHONE_AUTH_CREDENTIAL, credential)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        fireAuth = FirebaseAuth.getInstance()

        phoneNumber = intent?.getStringExtra(Constant.EXTRA_USER_PHONE)
        password = intent?.getStringExtra(Constant.EXTRA_USER_PASSWORD)
        verificationId = intent?.getStringExtra(Constant.EXTRA_VERIFICATION_ID)
        resendToken = intent?.getParcelableExtra(Constant.EXTRA_RESEND_TOKEN)
        authCredential = intent?.getParcelableExtra(Constant.EXTRA_USER_PHONE_AUTH_CREDENTIAL)

        sharedPref = SharedPref(this)

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        listTvOTPCode = arrayOf(tvNumber1, tvNumber2, tvNumber3, tvNumber4, tvNumber5, tvNumber6)

        val last3Num = phoneNumber?.substring(phoneNumber?.length?.minus(3)!!)
        tvTutorial.text = getString(R.string.text_verify_otp, last3Num)

        etInputCode.requestFocus()
        val keyboard =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(etInputCode, 0)

        startCountDown()

        etInputCode.addTextChangedListener(textWatcher)
        tvResend.setOnClickListener(this)

        if (authCredential != null) {
            CommonUtils.toggleLoading(window.decorView.rootView, true)
            signInWithPhoneAuthCredential(authCredential!!)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                listTvOTPCode[s.length - 1].text = s[s.length - 1].toString()
                if (s.length < 6) {
                    listTvOTPCode[s.length].text = ""
                } else {
                    verifyCode()
                }
            } else {
                listTvOTPCode[0].text = ""
            }
        }
    }

    private fun startCountDown() {
        tvResend.isEnabled = false
        tvResend.setTextColor(resources.getColor(R.color.colorPutty, null))
        if (countDown != null) {
            countDown?.cancel()
        }
        countDown = object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tvResend.text = getString(
                    R.string.label_button_resend_code_count_down,
                    millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                tvResend.isEnabled = true
                tvResend.setTextColor(resources.getColor(R.color.colorOrangish, null))
                tvResend.text = getString(R.string.label_button_resend_code)
            }

        }.start()
    }

    override fun onClick(v: View?) {
        when (v) {
            tvResend -> {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!,        // Phone number to verify
                    60,                   // Timeout duration
                    TimeUnit.SECONDS,     // Unit of timeout
                    this,                 // Activity (for callback binding)
                    callbacks,            // OnVerificationStateChangedCallbacks
                    resendToken           // ForceResendingToken from callbacks
                )
                startCountDown()
            }
        }
    }

    private fun verifyCode() {
        CommonUtils.toggleLoading(window.decorView.rootView, true)
        val credential = PhoneAuthProvider.getCredential(
            verificationId!!,
            etInputCode.text.toString().trim()
        )

        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        fireAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user

                    if (countDown != null) {
                        countDown?.cancel()
                    }

                    createNewAccount()
                } else {
                    CommonUtils.toggleLoading(window.decorView.rootView, false)
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        CommonUtils.hideKeyboard(this)
                        tvWarning.visibility = View.VISIBLE
                    }
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
            CommonUtils.hideKeyboard(this@VerifyPhoneActivity)
            tvWarning.text = e.message
            tvWarning.visibility = View.VISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
            this@VerifyPhoneActivity.verificationId = verificationId
            resendToken = token

            // ...
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDown != null) {
            countDown?.cancel()
        }
    }

    private fun createNewAccount() {
        try {
            val generatePrivateKey = EosPrivateKey()
            val publicKey = generatePrivateKey.publicKey.toString()
            val encryptedPrivateKey = AESCrypt.encrypt(
                generatePrivateKey.toWif(),
                getString(R.string.format_eascrypt_password, password)
            )

            userRegister(publicKey, encryptedPrivateKey)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun userRegister(publicKey: String, encryptedPrivateKey: String) {
        var data = HashMap<String, String>()
        data[Constant.API_FIELD_USER_IDENTITY] = phoneNumber!!
        data[Constant.API_FIELD_USER_PASSWORD] = password!!
        data[Constant.API_FIELD_PUBLIC_KEY] = publicKey
        data[Constant.API_FIELD_ENCRYPTED_PRIVATE_KEY] = encryptedPrivateKey
        data[Constant.API_FIELD_USER_ROLE] = Constant.USER_ROLE_COLLECTOR
        data[Constant.API_FIELD_EOS_NAME] = CommonUtils.generateEOSAccountName()
        data[Constant.API_FIELD_USER_NAME] = "Thomas Johnson"
        data[Constant.API_FIELD_USER_ADDRESS] = "Thomas Johnson"
//        data[Constant.API_FIELD_REGISTRATION_TOKEN] = sharedPref.getString(Constant.FCM_TOKEN)

        CommonUtils.toggleLoading(window.decorView.rootView, true)

        /*Create handle for the RetrofitInstance interface*/
        val call = service.create(AuthenticationApi::class.java).signUpApi(data)
        call.enqueue(object : Callback<SignUp> {

            override fun onResponse(call: Call<SignUp>, response: Response<SignUp>) {
                when {
                    response.code() == 201 ->
                        requestLogIn()

                    else -> {
                        CommonUtils.toggleLoading(window.decorView.rootView, false)
                        Toast.makeText(
                            this@VerifyPhoneActivity,
                            "Response Code ${response.code()}: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignUp>, t: Throwable) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                Toast.makeText(
                    this@VerifyPhoneActivity,
                    t.message,
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
                phoneNumber!!,
                password!!
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)

                    FinishVerifyActivity.start(this@VerifyPhoneActivity)

                } else {
                    Toast.makeText(this@VerifyPhoneActivity, response.errorBody()!!.string(), Toast.LENGTH_SHORT)
                        .show()
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                Toast.makeText(this@VerifyPhoneActivity, "Something went wrong when login", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }
}
