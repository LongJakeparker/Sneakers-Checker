package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.utils.CommonUtils
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SignIn
import kotlinx.android.synthetic.main.fragment_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    private var isShowingPassword: Boolean = false

    private lateinit var listIvPassCode: Array<ImageView>

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

        listIvPassCode = arrayOf(
            ivNumber1,
            ivNumber2,
            ivNumber3,
            ivNumber4,
            ivNumber5,
            ivNumber6
        )

        etUserPhone.addTextChangedListener(textWatcher)
        etUserPassword.addTextChangedListener(passwordTextWatcher)
        btnLogin.setOnClickListener(this)
//        btnShowPassword.setOnClickListener(this)
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

    private val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                listIvPassCode[s.length - 1].setImageResource(R.drawable.drawable_bg_pass_code_black)
                if (s.length < 6) {
                    listIvPassCode[s.length].setImageResource(R.drawable.drawable_bg_pass_code)
                } else {
                    btnLogin.isEnabled = validateData()
                }
            } else {
                listIvPassCode[0].setImageResource(R.drawable.drawable_bg_pass_code)
            }
        }
    }

    fun validateData(): Boolean {
        return !(etUserPhone.text.trim().length < 9 || etUserPassword.text.trim().length < 6)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnLogin -> requestLogIn()

//            R.id.btnShowPassword -> showPassword()

            R.id.root -> CommonUtils.hideKeyboard(activity)

            R.id.ibBack -> activity!!.finish()
        }
    }

//    private fun showPassword() {
//        val cursorStart = etUserPassword.selectionStart
//        val cursorEnd = etUserPassword.selectionEnd
//        if (!isShowingPassword) {
//            etUserPassword.transformationMethod = null
//            isShowingPassword = true
//            etUserPassword.setSelection(cursorStart, cursorEnd)
//            btnShowPassword.setImageResource(R.drawable.ic_hide_password)
//        } else {
//            etUserPassword.transformationMethod = PasswordTransformationMethod()
//            isShowingPassword = false
//            etUserPassword.setSelection(cursorStart, cursorEnd)
//            btnShowPassword.setImageResource(R.drawable.ic_show_password)
//        }
//    }

    private fun requestLogIn() {
        CommonUtils.toggleLoading(fragmentView, true)
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
                CommonUtils.toggleLoading(fragmentView, false)
                if (response.isSuccessful) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)
                    activity!!.setResult(Activity.RESULT_OK)
                    activity!!.finish()

                } else if (response.code() == 503) {
                    tvWarning.text = getString(R.string.msg_login_failed)
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
}