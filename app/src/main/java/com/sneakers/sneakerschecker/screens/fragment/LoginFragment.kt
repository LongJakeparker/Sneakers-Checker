package com.sneakers.sneakerschecker.screens.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.CommonUtils
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SignIn
import kotlinx.android.synthetic.main.fragment_login.*
import org.web3j.crypto.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null
    private lateinit var credentials: Credentials

    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    private lateinit var email: String
    private lateinit var privateKey: String

    private var isShowingPassword: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_login, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        privateKey = arguments?.getString(Constant.EXTRA_PRIVATE_KEY).toString()
        email = arguments?.getString(Constant.EXTRA_USER_EMAiL).toString()

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPassword.addTextChangedListener(textWatcher)
        btnLogin.setOnClickListener(this)
        btnShowPassword.setOnClickListener(this)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnLogin.isEnabled = etUserPassword.text.toString().isNotEmpty()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnLogin -> requestLogIn()

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

    private fun requestLogIn() {
        CommonUtils.toggleLoading(fragmentView, true)
        val authToken = okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                email,
                etUserPassword.text.toString().trim()
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.WALLET_USER)
                    credentials = Credentials.create(privateKey)
                    importPrivateKey()

                } else if (response.code() == 400) {
                    CommonUtils.toggleLoading(fragmentView, false)
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun importPrivateKey() {
        sharedPref.setCredentials(credentials, Constant.USER_CREDENTIALS)

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }
}