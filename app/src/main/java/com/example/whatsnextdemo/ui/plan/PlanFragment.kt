package com.example.whatsnextdemo.ui.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.whatsnextdemo.databinding.FragmentPlanBinding
import com.google.android.material.tabs.TabLayoutMediator

class PlanFragment : Fragment() {
    private var _binding: FragmentPlanBinding? = null
    private val binding: FragmentPlanBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPagerPlan.adapter = PlanPagerAdapter(this)
        TabLayoutMediator(binding.tabPlan, binding.viewPagerPlan) { tab, position ->
            tab.text = when (position) {
                PlanPagerAdapter.POSITION_REPORT -> "职业报告"
                else -> "行动计划"
            }
        }.attach()
        binding.viewPagerPlan.setCurrentItem(initialPage(), false)
    }

    override fun onDestroyView(): Unit {
        binding.viewPagerPlan.adapter = null
        super.onDestroyView()
        _binding = null
    }

    private fun initialPage(): Int {
        val page: Int = arguments?.getInt(ARG_INITIAL_PAGE) ?: PlanPagerAdapter.POSITION_REPORT
        return when (page) {
            PlanPagerAdapter.POSITION_ACTION_PLAN -> PlanPagerAdapter.POSITION_ACTION_PLAN
            else -> PlanPagerAdapter.POSITION_REPORT
        }
    }

    companion object {
        private const val ARG_INITIAL_PAGE: String = "initial_page"

        fun newInstance(initialPage: Int): PlanFragment {
            val fragment: PlanFragment = PlanFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_INITIAL_PAGE, initialPage)
            }
            return fragment
        }
    }
}
