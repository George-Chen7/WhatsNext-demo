package com.example.whatsnextdemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.ui.login.LoginActivity
import com.example.whatsnextdemo.utils.Constants

class ForceOfflineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Constants.ACTION_FORCE_OFFLINE) return

        SessionManager(context.applicationContext).clearLogin()
        Toast.makeText(
            context,
            "您的账号已在其他设备登录\n请重新登录",
            Toast.LENGTH_LONG
        ).show()

        val loginIntent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(loginIntent)
    }
}
