package com.sneakers.sneakerschecker.screens.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.sneakers.sneakerschecker.R

class InputPasswordDialog : DialogFragment(), View.OnClickListener {
    private lateinit var etFeedback: EditText
    private lateinit var btnSend: TextView

    companion object {
        fun show(fragmentManager: FragmentManager) {
            val dialog = InputPasswordDialog()
            dialog.isCancelable = false
            dialog.show(fragmentManager, InputPasswordDialog::class.java.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input_password, null, false)

//        etFeedback = view.findViewById(R.id.etFeedback)
//        btnSend = view.findViewById(R.id.tvSend)
//
//        etFeedback.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable) {}
//
//            override fun beforeTextChanged(s: CharSequence, start: Int,
//                                           count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int,
//                                       before: Int, count: Int) {
//                btnSend.isEnabled = s.isNotEmpty()
//                if (s.isNotEmpty()) {
//                    context?.resources?.getColor(R.color.blue_0F99FA)?.let { btnSend.setTextColor(it) }
//                } else {
//                    context?.resources?.getColor(R.color.gray_a6a6a6)?.let { btnSend.setTextColor(it) }
//                }
//            }
//        })
//
//        btnSend.setOnClickListener(this)
//        view.findViewById<View>(R.id.tvCancel).setOnClickListener(this)
//
        builder.setView(view)
        return builder.create()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
//            R.id.tvCancel -> {
//                SharePrefManager.setDateValue(SharePrefManager.LAST_APP_RATING, Date())
//                dismiss()
//            }
//
//            R.id.tvSend -> {
//                progressBar.visibility = View.VISIBLE
//                NailieRequest.sendFeedback2NailieSupport(etFeedback.text.toString().trim()) { result: Boolean?, e: ParseException? ->
//                    progressBar.visibility = View.GONE
//                    if (e == null && result!!) {
//                        SharePrefManager.setDateValue(SharePrefManager.LAST_APP_RATING, Date())
//                        (activity as  BaseActivity).showMessage(R.string.msg_thanks_for_feedback)
//                        dismiss()
//                    }
//                    else if (e != null) {
//                        if (e.code == 9001) {
//                            Toast.makeText(context, "Something failed", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            }
        }
    }
}