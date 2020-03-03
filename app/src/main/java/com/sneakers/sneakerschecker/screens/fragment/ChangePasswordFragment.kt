package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_change_password.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ChangePasswordFragment : BaseFragment() {
    private var fragmentView: View? = null
    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    private var user: User? = null

    private var newEncryptedPrivateKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = super.onCreateView(inflater, container, savedInstanceState)

        sharedPref = SharedPref(activity!!)

        user = sharedPref.getUser(Constant.LOGIN_USER)?.user

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etCurrentPasscode.addTextChangedListener(textWatcher)
        etNewPasscode.addTextChangedListener(textWatcher)
        etReenterNewPasscode.addTextChangedListener(textWatcher)

        btnChangePassword.setOnClickListener {
            if (validatePass()) {
                requestLogIn()
            }
        }

        root.setOnClickListener { CommonUtils.hideKeyboard(activity) }
    }

    private fun validatePass(): Boolean {
        if (etCurrentPasscode.text.toString().trim().length < 6
            || etNewPasscode.text.toString().trim().length < 6
            || etReenterNewPasscode.text.toString().trim().length < 6
        ) {

            val alertDialogFragment =
                AlertDialogFragment.newInstance(getString(R.string.msg_short_passcode))
            alertDialogFragment.show(
                fragmentManager!!,
                AlertDialogFragment::class.java.simpleName
            )

            return false
        }

        if (etReenterNewPasscode.text.toString().trim() != etNewPasscode.text.toString().trim()) {

            val alertDialogFragment =
                AlertDialogFragment.newInstance(getString(R.string.msg_new_passcode_not_match))
            alertDialogFragment.show(
                fragmentManager!!,
                AlertDialogFragment::class.java.simpleName
            )

            return false
        }

        return true
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
                user?.userIdentity!!,
                etCurrentPasscode.text.toString().trim(),
                fcmToken
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                if (response.isSuccessful) {
                    updateEncryptedPrivateKey()

                } else if (response.code() == 400) {
                    CommonUtils.toggleLoading(fragmentView, false)
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(getString(R.string.msg_login_failed_password))
                    alertDialogFragment.show(
                        fragmentManager!!,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun updateEncryptedPrivateKey() {
        val privateKey = AESCrypt.decrypt(
            user?.encryptedPrivateKey!!,
            getString(R.string.format_eascrypt_password, etCurrentPasscode.text.toString().trim())
        )
        newEncryptedPrivateKey = AESCrypt.encrypt(
            privateKey,
            getString(
                R.string.format_eascrypt_password,
                etReenterNewPasscode.text.toString().trim()
            )
        )

        updatePassword()
    }

    private fun updatePassword() {
        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val params = HashMap<String, Any?>()
        params[Constant.API_FIELD_OLD_PASSWORD] = etCurrentPasscode.text.toString().trim()
        params[Constant.API_FIELD_NEW_PASSWORD] = etReenterNewPasscode.text.toString().trim()
        params[Constant.API_FIELD_NEW_ENCRYPTED_PRIVATE_KEY] = newEncryptedPrivateKey

        val call = service.create(MainApi::class.java)
            .updatePassword(accessToken, user?.id!!, params)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.code() == 204) {
                    user?.encryptedPrivateKey = newEncryptedPrivateKey
                    val userInfo = sharedPref.getUser(Constant.LOGIN_USER)
                    userInfo?.user = user!!
                    sharedPref.setUser(userInfo!!, Constant.LOGIN_USER)

                    activity?.onBackPressed()
                } else {
                    CommonUtils.toggleLoading(fragmentView, false)
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            btnChangePassword.isEnabled = !etCurrentPasscode.text.toString().trim().isNullOrEmpty()
                    && !etNewPasscode.text.toString().trim().isNullOrEmpty()
                    && !etReenterNewPasscode.text.toString().trim().isNullOrEmpty()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_change_password
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_setting_change_password
    }
}