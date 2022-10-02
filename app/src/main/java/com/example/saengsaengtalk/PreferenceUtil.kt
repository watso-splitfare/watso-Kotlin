package com.example.saengsaengtalk

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cache", Context.MODE_PRIVATE)
    val editor = prefs.edit()

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        editor.putString(key, str).apply()
        editor.commit()
    }

    fun removeString(key: String){
        editor.remove(key)
        editor.commit()
    }
}