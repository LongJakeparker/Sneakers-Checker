package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_verify_phone.*

class VerifyPhoneFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private lateinit var listTvOTPCode: Array<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_verify_phone, container, false)

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listTvOTPCode = arrayOf(tvNumber1, tvNumber2, tvNumber3, tvNumber4, tvNumber5, tvNumber6)

        etInputCode.addTextChangedListener(textWatcher)
        root.setOnClickListener(this)
        ibBack.setOnClickListener(this)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                listTvOTPCode[s.length-1].text = s[s.length-1].toString()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.root -> CommonUtils.hideKeyboard(activity)

            R.id.ibBack -> activity?.onBackPressed()
        }
    }
}