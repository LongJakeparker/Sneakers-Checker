package com.sneakers.sneakerschecker.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SharedPref {
    private val PREFS_FILENAME = "com.sneakerchecker.storage"
    private var prefs: SharedPreferences? = null

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

    fun setUser(value: SignIn, fieldName: String) {
        val gson = Gson()
        val user = gson.toJson(value)

        val editor = prefs!!.edit()
        editor.putString(fieldName, user)
        editor.apply()
    }

    fun getUser(fieldName: String): SignIn {
        val gson = Gson()

        return gson.fromJson(prefs!!.getString(fieldName, ""), SignIn::class.java)
    }
}