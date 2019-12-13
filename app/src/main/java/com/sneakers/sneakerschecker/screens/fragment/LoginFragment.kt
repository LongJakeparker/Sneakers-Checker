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
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.PhoneAuthProvider
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.utils.CommonUtils
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SignIn
import kotlinx.android.synthetic.main.fragment_create_new.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.etUserPhone
import kotlinx.android.synthetic.main.fragment_login.ibBack
import kotlinx.android.synthetic.main.fragment_login.pickerCountryCode
import kotlinx.android.synthetic.main.fragment_login.root
import kotlinx.android.synthetic.main.fragment_login.tvWarning
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null
    private var password: String = ""

    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_login, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPhone.addTextChangedListener(textWatcher)
        btnLogin.setOnClickListener(this)
        root.setOnClickListener(this)
        ibBack.setOnClickListener(this)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnLogin.isEnabled = validateData()
        }
    }

    fun validateData(): Boolean {
        return etUserPhone.text.trim().length >= 9
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnLogin -> InputPasswordDialog.show(this, fragmentManager!!)

            R.id.root -> CommonUtils.hideKeyboard(activity)

            R.id.ibBack -> activity!!.finish()
        }
    }

    private fun requestLogIn() {
        CommonUtils.toggleLoading(fragmentView, true)
        val authToken =
            okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                password
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                CommonUtils.toggleLoading(fragmentView, false)
                if (response.isSuccessful) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)
                    activity!!.setResult(Activity.RESULT_OK)
                    activity!!.finish()

                } else if (response.code() == 503) {
                    tvWarning.text = getString(R.string.msg_login_failed_phone)
                    tvWarning.visibility = View.VISIBLE
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                } else if (response.code() == 400) {
                    tvWarning.text = getString(R.string.msg_login_failed_password)
                    tvWarning.visibility = View.VISIBLE
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                } else {
                    tvWarning.text = response.message()
                    tvWarning.visibility = View.VISIBLE
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            password = data?.extras?.getString(Constant.EXTRA_USER_PASSWORD, "")!!
            requestLogIn()
        }
    }
}