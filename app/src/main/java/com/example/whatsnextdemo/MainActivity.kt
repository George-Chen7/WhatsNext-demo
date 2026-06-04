package com.example.whatsnextdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.whatsnextdemo.databinding.ActivityMainBinding
import com.example.whatsnextdemo.ui.assessment.AssessmentFragment
import com.example.whatsnextdemo.ui.home.HomeFragment
import com.example.whatsnextdemo.ui.profile.ProfileFragment
import com.example.whatsnextdemo.ui.report.ReportFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment())
                R.id.nav_assessment -> showFragment(AssessmentFragment())
                R.id.nav_report -> showFragment(ReportFragment())
                R.id.nav_profile -> showFragment(ProfileFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
