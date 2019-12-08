package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant


class InputPasswordDialog : DialogFragment() {
    private lateinit var etInputCode: EditText
    private lateinit var listIvPassCode: Array<ImageView>

    companion object {
        fun show(fragment: Fragment, fragmentManager: FragmentManager) {
            val dialog = InputPasswordDialog()
            dialog.setTargetFragment(fragment, Constant.DIALOG_REQUEST_CODE)
            dialog.show(fragmentManager, InputPasswordDialog::class.java.simpleName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input_password, null, false)

        etInputCode = view.findViewById(R.id.etInputCode)
//        btnSend = view.findViewById(R.id.tvSend)

        listIvPassCode = arrayOf(
            view.findViewById(R.id.tvNumber1),
            view.findViewById(R.id.tvNumber2),
            view.findViewById(R.id.tvNumber3),
            view.findViewById(R.id.tvNumber4),
            view.findViewById(R.id.tvNumber5),
            view.findViewById(R.id.tvNumber6)
        )
//
        etInputCode.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty()) {
                    listIvPassCode[s.length - 1].setImageResource(R.drawable.drawable_bg_pass_code_black)
                    if (s.length < 6) {
                        listIvPassCode[s.length].setImageResource(R.drawable.drawable_bg_pass_code)
                    } else {
                        Handler().postDelayed({
                            returnPasscode()
                        }, 200)
                    }
                } else {
                    listIvPassCode[0].setImageResource(R.drawable.drawable_bg_pass_code)
                }
            }
        })

        etInputCode.requestFocus()
        val keyboard =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(etInputCode, 0)

        builder.setView(view)
        return builder.create()
    }

    private fun returnPasscode() {
        val bundle = Bundle()
        bundle.putString(Constant.EXTRA_USER_PASSWORD, etInputCode.text.toString().trim())

        val intent = Intent().putExtras(bundle)

        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)

        dismiss()
    }
}