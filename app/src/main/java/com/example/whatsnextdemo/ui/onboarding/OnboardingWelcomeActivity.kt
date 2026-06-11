package com.example.whatsnextdemo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsnextdemo.databinding.ActivityOnboardingWelcomeBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding

class OnboardingWelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        binding.btnStartPlan.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
    }
}
