package com.sneakers.sneakerschecker.model

import android.view.View
import com.sneakers.sneakerschecker.R

class CommonUtils {
    companion object {
        fun toggleLoading(loadingParent: View?, show: Boolean) {
            if (loadingParent == null) {
                return
            }
            val v = loadingParent.findViewById<View>(R.id.pb_loading)
            if (v != null)
                v.visibility = if (show) View.VISIBLE else View.GONE
        }
    }
}