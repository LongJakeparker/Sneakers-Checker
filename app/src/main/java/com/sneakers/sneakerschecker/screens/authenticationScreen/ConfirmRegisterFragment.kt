package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.fragment_confirm_register.*

class ConfirmRegisterFragment : Fragment() {
    private var fragmentView: View? = null

    private lateinit var sharedPref: SharedPref

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_confirm_register, container, false)

        sharedPref = context?.let { SharedPref(it) }!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserAddress.text =
            "0x" + sharedPref.getCredentials(Constant.USER_CREDENTIALS).ecKeyPair.privateKey.toString(16)

        btnGoToHome.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }

        btnCopyToClipBoard.setOnClickListener {
            copyToClipboard(tvUserAddress.text.toString())
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("simple text", text)
        clipboard.primaryClip = clip
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}