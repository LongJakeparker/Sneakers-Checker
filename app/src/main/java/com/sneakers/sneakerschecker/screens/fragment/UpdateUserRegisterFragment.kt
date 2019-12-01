package com.sneakers.sneakerschecker.screens.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_update_user_register.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class UpdateUserRegisterFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private lateinit var service: Retrofit

    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_update_user_register, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserName.addTextChangedListener(textWatcher)
        etUserAddress.addTextChangedListener(textWatcher)
        btnUpdate.setOnClickListener(this)
        root.setOnClickListener(this)
    }

    private fun updateUser() {
        CommonUtils.toggleLoading(fragmentView, true)
        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val userInfo = sharedPref.getUser(Constant.LOGIN_USER)
        val params = HashMap<String, Any>()
        params[Constant.API_FIELD_USER_NAME] = etUserName.text.toString().trim()
        params[Constant.API_FIELD_USER_ADDRESS] = etUserAddress.text.toString().trim()

        val call = service.create(MainApi::class.java)
            .updateUser(accessToken, userInfo?.user?.id!!, params)
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
                CommonUtils.toggleLoading(fragmentView, false)
                if (response.code() == 204) {
                    userInfo.user.username = etUserName.text.toString().trim()
                    userInfo.user.address = etUserAddress.text.toString().trim()
                    sharedPref.setUser(userInfo, Constant.LOGIN_USER)

                    val intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }

    override fun onClick(v: View?) {
        when (v) {
            btnUpdate -> updateUser()

            root -> CommonUtils.hideKeyboard(activity)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnUpdate.isEnabled = validateData()
        }
    }

    fun validateData(): Boolean {
        return !(etUserName.text.toString().trim().isEmpty() || etUserAddress.text.toString().trim().isEmpty())
    }
}