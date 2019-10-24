package com.sneakers.sneakerschecker.`interface`

import android.os.Bundle

interface IDialogListener {
    fun onDialogFinish(tag: String, ok: Boolean, result: Bundle)

    fun onDialogCancel(tag: String)
}