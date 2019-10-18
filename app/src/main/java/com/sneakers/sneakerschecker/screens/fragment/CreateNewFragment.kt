package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.*
import kotlinx.android.synthetic.main.fragment_create_new.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.security.Security


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/**
 * A simple [Fragment] subclass.
 *
 */
class CreateNewFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private lateinit var service: Retrofit
    private lateinit var sharedPref: SharedPref
    private lateinit var credentials: Credentials

    private var isShowingPassword: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_new, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        setupBouncyCastle()

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserName.addTextChangedListener(textWatcher)
        etUserEmail.addTextChangedListener(textWatcher)
        etUserPassword.addTextChangedListener(textWatcher)
        btnRegister.setOnClickListener(this)
        ibBack.setOnClickListener(this)
        btnShowPassword.setOnClickListener(this)
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider.javaClass == BouncyCastleProvider::class.java) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
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
        when (v!!.id) {
            R.id.btnRegister -> createNewAccount()

            R.id.ibBack -> activity?.onBackPressed()

            R.id.btnShowPassword -> showPassword()
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

    private fun callValidateEmail() {
        val email = etUserEmail.text.toString()

        if (!Validation.validateEmail(email)!!) {
            visibleWarning(getString(R.string.text_message_input_valid_email))
        } else {
            tvWarning.visibility = GONE
        }
    }

    fun validateData(): Boolean {
        return if (etUserName.text.isEmpty() || etUserEmail.text.isEmpty() || etUserPassword.text.isEmpty()) {
            false
        } else {
            callValidateEmail()
            Validation.validateEmail(etUserEmail.text.toString())!!
        }
    }

    private fun createNewAccount() {
        try {
            val keyPair = Keys.createEcKeyPair()
            credentials = Credentials.create(keyPair)

            userRegister()
        } catch (e: Exception) {
            Log.e("Error: ", e.message)
        }
    }

    private fun userRegister() {
        var data = HashMap<String, String>()
        data[Constant.API_FIELD_USER_NAME] = etUserName.text.toString()
        data[Constant.API_FIELD_USER_EMAIL] = etUserEmail.text.toString()
        data[Constant.API_FIELD_USER_PASSWORD] = etUserPassword.text.toString()
        data[Constant.API_FIELD_NETWORK_ADDRESS] = credentials.address
        data[Constant.API_FIELD_REGISTRATION_TOKEN] = sharedPref.getString(Constant.FCM_TOKEN)

        CommonUtils.toggleLoading(fragmentView, true)

        /*Create handle for the RetrofitInstance interface*/
        val call = service.create(AuthenticationApi::class.java).signUpApi(data)
        call.enqueue(object : Callback<SignUp> {

            override fun onResponse(call: Call<SignUp>, response: Response<SignUp>) {
                when {
                    response.code() == 201 -> //newAccount(response.body()!!.passwordHash)
                        requestLogIn()
                    response.code() == 400 -> {
                        CommonUtils.toggleLoading(fragmentView, false)
                        visibleWarning("Email has been used")
                    }
                    else -> {
                        CommonUtils.toggleLoading(fragmentView, false)
                        Toast.makeText(context, "Response Code: " + response.code(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignUp>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestLogIn() {

        val authToken = okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                etUserEmail.text.toString(),
                etUserPassword.text.toString()
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                CommonUtils.toggleLoading(fragmentView, false)

                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.WALLET_USER)
                    sharedPref.setCredentials(credentials, Constant.USER_CREDENTIALS)

                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
                } else if (response.code() == 400) {
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun visibleWarning(message: String) {
        tvWarning.text = "Oops!\n$message"
        tvWarning.visibility = VISIBLE
    }
}
