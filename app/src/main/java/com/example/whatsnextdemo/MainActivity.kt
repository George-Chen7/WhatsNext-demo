package com.example.whatsnextdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.whatsnextdemo.databinding.ActivityMainBinding
import com.example.whatsnextdemo.ui.assessment.AssessmentFragment
import com.example.whatsnextdemo.ui.home.HomeFragment
import com.example.whatsnextdemo.ui.plan.PlanFragment
import com.example.whatsnextdemo.ui.plan.PlanPagerAdapter
import com.example.whatsnextdemo.ui.profile.ProfileFragment
import com.example.whatsnextdemo.utils.applySystemBarPadding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pendingPlanPage: Int = PlanPagerAdapter.POSITION_REPORT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment())
                R.id.nav_assessment -> showFragment(AssessmentFragment())
                R.id.nav_plan -> {
                    val page: Int = pendingPlanPage
                    pendingPlanPage = PlanPagerAdapter.POSITION_REPORT
                    showFragment(PlanFragment.newInstance(page))
                }
                R.id.nav_profile -> showFragment(ProfileFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = when (intent.getStringExtra(EXTRA_START_TAB)) {
                TAB_REPORT -> R.id.nav_plan
                TAB_PLAN -> R.id.nav_plan
                else -> R.id.nav_home
            }
        }
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    fun openAssessmentTab(): Unit {
        binding.bottomNavigation.selectedItemId = R.id.nav_assessment
    }

    fun openReportTab(): Unit {
        openPlanTab()
    }

    fun openPlanTab(): Unit {
        pendingPlanPage = PlanPagerAdapter.POSITION_REPORT
        binding.bottomNavigation.selectedItemId = R.id.nav_plan
    }

    fun openActionPlanTab(): Unit {
        pendingPlanPage = PlanPagerAdapter.POSITION_ACTION_PLAN
        if (binding.bottomNavigation.selectedItemId == R.id.nav_plan) {
            showFragment(PlanFragment.newInstance(PlanPagerAdapter.POSITION_ACTION_PLAN))
        } else {
            binding.bottomNavigation.selectedItemId = R.id.nav_plan
        }
    }

    companion object {
        const val EXTRA_START_TAB = "start_tab"
        const val TAB_REPORT = "report"
        const val TAB_PLAN = "plan"
    }
}
