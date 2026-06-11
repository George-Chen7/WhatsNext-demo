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

    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean(userKey(Constants.KEY_HAS_COMPLETED_ONBOARDING), false)
    }

    fun setProfileCompleted() {
        preferences.edit()
            .putBoolean(userKey(Constants.KEY_HAS_COMPLETE_PROFILE), true)
            .apply()
    }

    fun setAssessmentCompleted() {
        preferences.edit()
            .putBoolean(userKey(Constants.KEY_HAS_COMPLETED_ASSESSMENT), true)
            .apply()
    }

    fun setFirstReportGenerated() {
        preferences.edit()
            .putBoolean(userKey(Constants.KEY_HAS_GENERATED_FIRST_REPORT), true)
            .putBoolean(userKey(Constants.KEY_HAS_COMPLETED_ONBOARDING), true)
            .apply()
    }

    private fun userKey(key: String): String {
        val username = getUsername().ifBlank { "guest" }
        return "${key}_$username"
    }
}
