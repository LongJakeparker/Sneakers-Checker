package com.sneakers.sneakerschecker.screens.fragment

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.fragment_verify_phone.*


class VerifyPhoneFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private lateinit var listTvOTPCode: Array<TextView>
    private lateinit var sharedPref: SharedPref
    private var countDown: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_verify_phone, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listTvOTPCode = arrayOf(tvNumber1, tvNumber2, tvNumber3, tvNumber4, tvNumber5, tvNumber6)

        val phoneNumber = sharedPref.getUser(Constant.LOGIN_USER)?.user?.email
        val last3Num = phoneNumber?.substring(phoneNumber.length.minus(3))
        tvTutorial.text = getString(R.string.text_verify_otp, last3Num)

        etInputCode.requestFocus()
        val keyboard =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(etInputCode, 0)

        startCountDown()

        etInputCode.addTextChangedListener(textWatcher)
        tvResend.setOnClickListener(this)
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
                    btnVerify.isEnabled = false
                } else {
                    btnVerify.isEnabled = true
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
        when (v?.id) {
            R.id.tvResend -> startCountDown()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDown != null) {
            countDown?.cancel()
        }
    }
}