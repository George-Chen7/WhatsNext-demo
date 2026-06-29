package com.example.whatsnextdemo.ui.plan

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsnextdemo.ui.report.ReportFragment

class PlanPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return PAGE_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POSITION_REPORT -> ReportFragment()
            POSITION_ACTION_PLAN -> ActionPlanFragment()
            else -> throw IllegalArgumentException("Unknown plan page position: $position")
        }
    }

    companion object {
        const val POSITION_REPORT: Int = 0
        const val POSITION_ACTION_PLAN: Int = 1
        private const val PAGE_COUNT: Int = 2
    }
}
