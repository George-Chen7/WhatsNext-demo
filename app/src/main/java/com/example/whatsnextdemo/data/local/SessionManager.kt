package com.example.whatsnextdemo.data.local

import android.content.Context
import com.example.whatsnextdemo.utils.Constants

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    fun saveLogin(username: String) {
        preferences.edit()
            .putBoolean(Constants.KEY_IS_LOGIN, true)
            .putString(Constants.KEY_USERNAME, username)
            .putString(Constants.KEY_TOKEN, "local-token-$username")
            .apply()
    }

    fun clearLogin() {
        preferences.edit()
            .putBoolean(Constants.KEY_IS_LOGIN, false)
            .remove(Constants.KEY_USERNAME)
            .remove(Constants.KEY_TOKEN)
            .apply()
    }

    fun isLogin(): Boolean {
        return preferences.getBoolean(Constants.KEY_IS_LOGIN, false)
    }

    fun getUsername(): String {
        return preferences.getString(Constants.KEY_USERNAME, "") ?: ""
    }
}
