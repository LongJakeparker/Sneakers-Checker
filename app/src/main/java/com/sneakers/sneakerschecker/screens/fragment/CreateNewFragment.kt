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
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.eosCommander.crypto.ec.EosPrivateKey
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_create_new.*
import org.web3j.crypto.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


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
        btnShowPassword.setOnClickListener(this)
        root.setOnClickListener(this)
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

            R.id.root -> CommonUtils.hideKeyboard(activity)
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
        return !(etUserPhone.text.isEmpty() || etUserPassword.text.length < 6)
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
        var data = HashMap<String, String>()
        data[Constant.API_FIELD_USER_EMAIL] =
            pickerCountryCode.selectedCountryCode + etUserPhone.text.toString()
        data[Constant.API_FIELD_USER_PASSWORD] = etUserPassword.text.toString()
        data[Constant.API_FIELD_USER_ADDRESS] = "New York"
        data[Constant.API_FIELD_PUBLIC_KEY] = publicKey
        data[Constant.API_FIELD_ENCRYPTED_PRIVATE_KEY] = encryptedPrivateKey
        data[Constant.API_FIELD_USER_ROLE] = Constant.USER_ROLE_COLLECTOR
        data[Constant.API_FIELD_USER_NAME] = "Test Mobile"
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
                pickerCountryCode.selectedCountryCode + etUserPhone.text.toString(),
                etUserPassword.text.toString()
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                CommonUtils.toggleLoading(fragmentView, false)

                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)

                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(
                        R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left,
                        R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right
                    )
                        .replace(R.id.fl_create_content, VerifyPhoneFragment())
                        .addToBackStack(null)
                        .commit()
                } else if (response.code() == 400) {
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
}
