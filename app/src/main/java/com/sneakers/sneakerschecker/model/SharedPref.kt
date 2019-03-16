package com.sneakers.sneakerschecker.model

import android.content.Context
import android.content.SharedPreferences

class SharedPref {
    val PREFS_FILENAME = "com.sneakerchecker.storage"
    var prefs: SharedPreferences? = null

    constructor(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
    }

    fun getInt(fieldName: String): Int {
        return prefs!!.getInt(fieldName, 0)
    }

    fun setInt(value: Int, fieldName: String) {
        val editor = prefs!!.edit()
        editor.putInt(fieldName, value)
        editor.apply()
    }

    fun getString(fieldName: String): String {
        return prefs!!.getString(fieldName, "")
    }

    fun setString(value: String, fieldName: String) {
        val editor = prefs!!.edit()
        editor.putString(fieldName, value)
        editor.apply()
    }
}