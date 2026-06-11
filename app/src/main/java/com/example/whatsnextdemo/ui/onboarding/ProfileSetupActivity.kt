package com.example.whatsnextdemo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.ActivityProfileSetupBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        sessionManager = SessionManager(this)
        userRepository = UserRepository(AppDatabase.getInstance(this).userDao())
        preloadUser()

        binding.btnNextAssessment.setOnClickListener {
            saveProfile()
        }
    }

    private fun preloadUser() {
        lifecycleScope.launch {
            val username = sessionManager.getUsername()
            val user = withContext(Dispatchers.IO) {
                userRepository.findUser(username)
            }
            binding.etNickname.setText(user?.nickname.orEmpty().ifBlank { username })
            binding.etMajor.setText(user?.major.orEmpty())
        }
    }

    private fun saveProfile() {
        val username = sessionManager.getUsername()
        val nickname = binding.etNickname.text?.toString()?.trim().orEmpty()
        val major = binding.etMajor.text?.toString()?.trim().orEmpty()

        if (nickname.isEmpty() || major.isEmpty()) {
            Toast.makeText(this, "请完整填写昵称和专业", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnNextAssessment.isEnabled = false
        lifecycleScope.launch {
            val updated = withContext(Dispatchers.IO) {
                userRepository.updateProfile(username, nickname, major)
            }
            binding.btnNextAssessment.isEnabled = true
            if (!updated) {
                Toast.makeText(this@ProfileSetupActivity, "用户信息不存在，请重新登录", Toast.LENGTH_SHORT).show()
                return@launch
            }

            sessionManager.setProfileCompleted()
            startActivity(Intent(this@ProfileSetupActivity, AssessmentGuideActivity::class.java))
        }
    }
}
