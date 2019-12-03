package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.screens.activity.CustomScanActivity
import kotlinx.android.synthetic.main.fragment_create_transfer.*

class CreateTransferFragment : Fragment(), View.OnClickListener {
    private val REQUEST_CODE_SCAN_EOS_NAME = 1002

    private var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_transfer, container, false)

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener(this)
        ivScanEosName.setOnClickListener(this)
        etReceiverEosName.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                ivScanEosName.setImageResource(R.drawable.ic_close_has_bg)
            } else {
                ivScanEosName.setImageResource(R.drawable.ic_scan_has_bg)
            }
            btnContinue.isEnabled = validateData()
        }
    }

    private fun validateData(): Boolean {
        return !(etReceiverEosName.text.isEmpty())
    }

    override fun onClick(v: View?) {
        when (v) {
            btnClose -> activity?.finish()

            ivScanEosName -> {
                if (etReceiverEosName.text.isEmpty()) {
                    CustomScanActivity.startForResult(this, CustomScanActivity.ScanType.SCAN_EOS_NAME, REQUEST_CODE_SCAN_EOS_NAME)
                } else {
                    etReceiverEosName.text.clear()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == REQUEST_CODE_SCAN_EOS_NAME && resultCode == Activity.RESULT_OK -> {
                val eosName = data?.extras?.getString(Constant.EXTRA_USER_EOS_NAME)
                etReceiverEosName.setText(eosName)
                ivScanEosName.setImageResource(R.drawable.ic_close_has_bg)
            }
        }
    }
}