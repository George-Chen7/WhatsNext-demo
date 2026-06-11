package com.example.whatsnextdemo.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.ActivityRegisterBinding
import com.example.whatsnextdemo.ui.onboarding.OnboardingWelcomeActivity
import com.example.whatsnextdemo.utils.applySystemBarPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        sessionManager = SessionManager(this)
        userRepository = UserRepository(AppDatabase.getInstance(this).userDao())

        binding.btnRegister.setOnClickListener { register() }
        binding.tvGoLogin.setOnClickListener { finish() }
    }

    private fun register() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                userRepository.register(username, password)
            }
            if (result.isSuccess) {
                sessionManager.saveLogin(username)
                Toast.makeText(this@RegisterActivity, "注册成功，请完善个人信息", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, OnboardingWelcomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@RegisterActivity, result.exceptionOrNull()?.message ?: "注册失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
