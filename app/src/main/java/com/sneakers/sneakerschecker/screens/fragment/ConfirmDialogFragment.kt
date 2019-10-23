package com.sneakers.sneakerschecker.screens.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.`interface`.IDialogListener
import com.sneakers.sneakerschecker.model.CommonUtils

class ConfirmDialogFragment : DialogFragment() {
    private var mMessage: CharSequence? = null
    private var mTitle: CharSequence? = null
    private var mPositiveLabel: String? = null
    private var mNegativeLabel: String? = null
    private var mListener: IDialogListener? = null
    private var isBoldTitle: Boolean = false

    companion object {

        private val KEY_MESSAGE = "key_message"
        private val KEY_TITLE = "key_title"
        private val KEY_IS_BOLD_TITLE = "is_bold_title"
        private val KEY_POSITIVE_LABEL = "key_positive_label"
        private val KEY_NEGATIVE_LABEL = "key_negative_label"

        fun newInstance(title: CharSequence, message: CharSequence, isBoldTitle: Boolean): ConfirmDialogFragment {
            val dialogFragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_TITLE, title)
            bundle.putCharSequence(KEY_MESSAGE, message)
            bundle.putBoolean(KEY_IS_BOLD_TITLE, isBoldTitle)
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(title: CharSequence, message: CharSequence): ConfirmDialogFragment {
            val dialogFragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_TITLE, title)
            bundle.putCharSequence(KEY_MESSAGE, message)
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(
            title: CharSequence,
            message: CharSequence,
            positiveLabel: String,
            negativeLabel: String
        ): ConfirmDialogFragment {
            val dialogFragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_TITLE, title)
            bundle.putCharSequence(KEY_MESSAGE, message)
            bundle.putString(KEY_POSITIVE_LABEL, positiveLabel)
            bundle.putString(KEY_NEGATIVE_LABEL, negativeLabel)
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(message: CharSequence): ConfirmDialogFragment {
            val dialogFragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_MESSAGE, message)
            dialogFragment.arguments = bundle
            return dialogFragment
        }

        fun newInstance(message: CharSequence, positiveLabel: String, negativeLabel: String): ConfirmDialogFragment {
            val dialogFragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.putCharSequence(KEY_MESSAGE, message)
            bundle.putString(KEY_POSITIVE_LABEL, positiveLabel)
            bundle.putString(KEY_NEGATIVE_LABEL, negativeLabel)
            dialogFragment.arguments = bundle
            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        mMessage = bundle?.getCharSequence(KEY_MESSAGE)
        mTitle = bundle?.getCharSequence(KEY_TITLE)
        mPositiveLabel = bundle?.getString(KEY_POSITIVE_LABEL)
        mNegativeLabel = bundle?.getString(KEY_NEGATIVE_LABEL)
        isBoldTitle = bundle?.getBoolean(KEY_IS_BOLD_TITLE, false)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        if (!TextUtils.isEmpty(mMessage)) {
            builder.setMessage(mMessage)
        }
        if (!TextUtils.isEmpty(mTitle)) {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_custom_title, null)
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            if (isBoldTitle)
                tvTitle.setTypeface(null, Typeface.BOLD)
            tvTitle.text = mTitle
            builder.setCustomTitle(view)
        }
        val positiveLabel = if (!TextUtils.isEmpty(mPositiveLabel)) mPositiveLabel else getString(R.string.common_OK)
        builder.setPositiveButton(positiveLabel) { dialog, which ->
            if (mListener != null) {
                tag?.let { mListener!!.onDialogFinish(it, true, Bundle()) }
            }
            dismiss()
        }

        val negativeLabel =
            if (!TextUtils.isEmpty(mNegativeLabel)) mNegativeLabel else getString(R.string.common_cancel)
        builder.setNegativeButton(negativeLabel) { dialog, which ->
            if (mListener != null) {
                tag?.let { mListener!!.onDialogFinish(it, false, Bundle()) }
            }
            dismiss()
        }
        return builder.create()
    }

    fun setListener(listener: IDialogListener) {
        mListener = listener
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (mListener != null) {
            mListener!!.onDialogCancel(this.javaClass.simpleName)
        }
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val positiveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            if (positiveButton != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    positiveButton.setTextColor(resources.getColor(R.color.colorDarkTurquoise, null))
                }
                else {
                    positiveButton.setTextColor(resources.getColor(R.color.colorDarkTurquoise))
                }
                positiveButton.textSize = 14f
                CommonUtils.setTextBold(positiveButton)
            }

            val negativeButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            if (negativeButton != null) {
                negativeButton.textSize = 14f
            }

            val messageView = (dialog as AlertDialog).findViewById<TextView>(android.R.id.message)
            if (messageView != null) {
                messageView.textSize = 14f
                messageView.gravity = Gravity.LEFT
            }

        }
    }
}
