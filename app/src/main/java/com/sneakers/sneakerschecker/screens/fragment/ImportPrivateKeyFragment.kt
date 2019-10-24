package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.CheckPrivateKeyResultModel
import com.sneakers.sneakerschecker.model.CommonUtils
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.screens.activity.CustomScanActivity
import kotlinx.android.synthetic.main.fragment_import_private_key.*
import org.web3j.crypto.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ImportPrivateKeyFragment : Fragment(), View.OnClickListener {
    private val START_SCAN_PRIVATE_KEY = 1000

    private var fragmentView: View? = null
    private lateinit var credentials: Credentials

    private lateinit var service: Retrofit
    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_import_private_key, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserPrivateKey.addTextChangedListener(textWatcher)
        btnScanPrivateKey.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        tv_question_1.setOnClickListener(this)
        tv_question_2.setOnClickListener(this)
        tv_question_3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnScanPrivateKey -> CustomScanActivity.startForResult(activity!!, CustomScanActivity.ScanType.SCAN_PRIVATE_KEY, START_SCAN_PRIVATE_KEY)

            R.id.btnNext -> checkPrivateKey()

            R.id.btnBack -> activity?.onBackPressed()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            btnNext.isEnabled = etUserPrivateKey.text.toString().isNotEmpty()
        }
    }

    private fun checkPrivateKey() {
        CommonUtils.toggleLoading(fragmentView, true)
        try {
            credentials = Credentials.create(etUserPrivateKey.text.toString().trim())
            tvWarning.visibility = GONE
        }
        catch (e: Exception) {
            tvWarning.text = "Wrong key format"
            tvWarning.visibility = VISIBLE
            CommonUtils.toggleLoading(fragmentView, false)
            return
        }
        var data = HashMap<String, String>()
        data["registrationToken"] = sharedPref.getString(Constant.FCM_TOKEN)
        val call = service.create(AuthenticationApi::class.java)
            .restoration(
                credentials.address,
                data
            )
        call.enqueue(object : Callback<CheckPrivateKeyResultModel> {

            override fun onResponse(
                call: Call<CheckPrivateKeyResultModel>,
                response: Response<CheckPrivateKeyResultModel>
            ) {
                CommonUtils.toggleLoading(fragmentView, false)
                if (response.code() == 200) {
                    goToLogin(response.body()!!.email)

                } else {
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                    tvWarning.text = resources.getText(R.string.text_message_incorrect_key)
                    tvWarning.visibility = VISIBLE
                }
            }

            override fun onFailure(call: Call<CheckPrivateKeyResultModel>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun goToLogin(email: String) {
        val bundle = Bundle()
        bundle.putString(Constant.EXTRA_PRIVATE_KEY, etUserPrivateKey.text.toString().trim())
        bundle.putString(Constant.EXTRA_USER_EMAiL, email)

        val loginFragment = LoginFragment()
        loginFragment.arguments = bundle

        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_login_content, loginFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == START_SCAN_PRIVATE_KEY && resultCode == Activity.RESULT_OK -> {
                etUserPrivateKey.setText(data?.getStringExtra(Constant.EXTRA_PRIVATE_KEY))
            }
        }
    }
}