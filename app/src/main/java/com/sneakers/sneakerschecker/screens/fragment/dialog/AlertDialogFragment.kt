package com.sneakers.sneakerschecker.screens.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.`interface`.IDialogListener

class AlertDialogFragment : DialogFragment() {
    private var mTitle: CharSequence? = null
    private var mMessage: CharSequence? = null
    private var mCancelTouchOutside = true
    private var mListener: IDialogListener? = null

    companion object {
        private const val KEY_MESSAGE = "key_message"
        private const val KEY_TITLE = "key_title"
        private const val KEY_CANCEL_TOUCH_OUT_SIDE = "key_cancel_touch_out_side"
        fun newInstance(message: CharSequence?): AlertDialogFragment {
            val dialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_MESSAGE, message)
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(
            message: CharSequence?,
            cancelTouchOutside: Boolean
        ): AlertDialogFragment {
            val dialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_MESSAGE, message)
            bundle.putBoolean(
                KEY_CANCEL_TOUCH_OUT_SIDE,
                cancelTouchOutside
            )
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(
            title: CharSequence?,
            message: CharSequence?,
            cancelTouchOutside: Boolean
        ): AlertDialogFragment {
            val dialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_TITLE, title)
            bundle.putCharSequence(KEY_MESSAGE, message)
            bundle.putBoolean(
                KEY_CANCEL_TOUCH_OUT_SIDE,
                cancelTouchOutside
            )
            dialogFragment.arguments = bundle
            return dialogFragment
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTitle = arguments?.getCharSequence(KEY_TITLE)
        mMessage = arguments?.getCharSequence(KEY_MESSAGE)
        mCancelTouchOutside = arguments?.getBoolean(KEY_CANCEL_TOUCH_OUT_SIDE, true)!!
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        if (!TextUtils.isEmpty(mMessage)) {
            builder.setMessage(mMessage)
        }
        builder.setPositiveButton(
            getString(R.string.common_OK)
        ) { dialog, which ->
            if (mListener != null) {
                mListener?.onDialogFinish(tag!!, true, Bundle())
            }
            dismiss()
        }
        if (!TextUtils.isEmpty(mTitle)) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.layout_custom_title, null)
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            tvTitle.setTypeface(null, Typeface.BOLD)
            tvTitle.text = mTitle
            builder.setCustomTitle(view)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(mCancelTouchOutside)
        return alertDialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (mListener != null) {
            mListener?.onDialogCancel(javaClass.simpleName)
        }
    }

    fun setListener(listener: IDialogListener?) {
        mListener = listener
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val messageView = dialog?.findViewById<TextView>(R.id.message)
            if (messageView != null) {
                messageView.textSize = 14f
                messageView.gravity = Gravity.CENTER
                messageView.setPadding(5, 5, 5, 5)
            }
            val positiveButton: Button = (dialog as AlertDialog).getButton(
                DialogInterface.BUTTON_POSITIVE
            )
            if (positiveButton != null) {
                positiveButton.textSize = 14f
            }
            val negativeButton: Button = (dialog as AlertDialog).getButton(
                DialogInterface.BUTTON_NEGATIVE
            )
            if (negativeButton != null) {
                negativeButton.textSize = 14f
            }
        }
    }
}