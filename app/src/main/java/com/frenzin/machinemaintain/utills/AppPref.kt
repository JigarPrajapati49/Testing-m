package com.frenzin.machinemaintain.utills

import android.content.Context
import android.content.SharedPreferences


object AppPref {
    private var sPref: SharedPreferences? = null
    private var sEditor: SharedPreferences.Editor? = null

    fun getToken(): String? {
        return sPref!!.getString(PreferenceKeys.USER_TOKEN, "")
    }
    fun getInstance(context: Context): AppPref {
        if (sPref == null) {
            sPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            sEditor = sPref?.edit()
        }
        return this
    }

    //set methods
    fun set(key: String, value: String) {
        sEditor?.putString(key, value)?.commit()
    }

    fun set(key: String, value: Boolean) {
        sEditor?.putBoolean(key, value)?.commit()
    }

    fun set(key: String, value: Float) {
        sEditor?.putFloat(key, value)?.commit()
    }

    fun set(key: String, value: Int) {
        sEditor?.putInt(key, value)?.commit()
    }

    fun set(key: String, value: Long) {
        sEditor?.putLong(key, value)?.commit()
    }

    fun set(key: String, value: Set<String>) {
        sEditor?.putStringSet(key, value)?.commit()
    }

    // get methods
    fun getInt(key: String, defaultVal: Int): Int {
        return sPref?.getInt(key, defaultVal) ?: defaultVal
    }

    fun getInt(key: String): Int {
        return sPref?.getInt(key, 0) ?: 0
    }

    fun getString(key: String, defaultVal: String): String {
        return sPref?.getString(key, defaultVal) ?: defaultVal
    }

    fun getString(key: String): String {
        return sPref?.getString(key, "") ?: ""
    }

    fun getBoolean(key: String, defaultVal: Boolean): Boolean {
        return sPref?.getBoolean(key, defaultVal) ?: defaultVal
    }

    fun getBoolean(key: String): Boolean {
        return sPref?.getBoolean(key, false) ?: false
    }

    fun getFloat(key: String, defaultVal: Float): Float {
        return sPref?.getFloat(key, defaultVal) ?: defaultVal
    }

    fun getFloat(key: String): Float {
        return sPref?.getFloat(key, 0f) ?: 0f
    }

    fun getLong(key: String, defaultVal: Long): Long {
        return sPref?.getLong(key, defaultVal) ?: defaultVal
    }

    fun getLong(key: String): Long {
        return sPref?.getLong(key, 0) ?: 0
    }

    fun getStringSet(key: String): Set<String>? {
        return sPref?.getStringSet(key, null)
    }

    fun contains(key: String): Boolean {
        return sPref?.contains(key) ?: false
    }

    fun remove(key: String) {
        sEditor?.remove(key)?.apply()
    }

    fun getAll(): Map<String, *>? {
        return sPref?.all
    }

    fun isLogin(): Boolean {
        return getBoolean(PreferenceKeys.IS_LOGIN)
    }

    fun clearSession() {
        val token = getString(PreferenceKeys.FCM_TOKEN)
        sEditor?.remove(PreferenceKeys.USER_TOKEN)
        sEditor?.remove(PreferenceKeys.IS_LOGIN)
        sEditor?.clear()?.apply()
        set(PreferenceKeys.FCM_TOKEN, token)
    }


}