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
import android.widget.Toast
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SignIn
import com.sneakers.sneakerschecker.screens.activity.CreateNewActivity
import com.sneakers.sneakerschecker.screens.fragment.dialog.InputPasswordDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginFragment : BaseFragment(), View.OnClickListener {

    private var fragmentView: View? = null
    private var password: String = ""

    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_log_in
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPref = SharedPref(activity!!)

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPhone.addTextChangedListener(textWatcher)
        btnLogin.setOnClickListener(this)
        root.setOnClickListener(this)
        llGotoSignup.setOnClickListener(this)
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
        return etUserPhone.text.isNotEmpty()
    }

    override fun onClick(v: View?) {
        when (v) {

            btnLogin -> InputPasswordDialogFragment.show(this, fragmentManager!!)

            root -> CommonUtils.hideKeyboard(activity)

            llGotoSignup -> CreateNewActivity.start(activity!!)
        }
    }

    private fun requestLogIn() {
        CommonUtils.toggleLoading(fragmentView, true)
        val fcmToken = sharedPref.getString(Constant.FCM_TOKEN)
        val authToken =
            okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                "+${pickerCountryCode.selectedCountryCode + etUserPhone.text.toString().trim()}",
                password,
                fcmToken
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
            CommonUtils.hideKeyboard(activity!!)
            password = data?.extras?.getString(Constant.EXTRA_USER_PASSWORD, "")!!
            requestLogIn()
        }
    }
}