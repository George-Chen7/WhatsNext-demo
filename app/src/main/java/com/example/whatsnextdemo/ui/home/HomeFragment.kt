package com.example.whatsnextdemo.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.BannerItem
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var assessmentRepository: AssessmentRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        assessmentRepository = AssessmentRepository(
            AppDatabase.getInstance(requireContext()).assessmentResultDao()
        )

        setupHeader()
        setupBanner()
        loadLatestAssessment()
    }

    private fun setupHeader() {
        val username = sessionManager.getUsername().ifBlank { "同学" }
        binding.tvWelcome.text = "你好，$username"
        binding.tvAvatar.text = username.take(1).uppercase()
    }

    private fun setupBanner() {
        binding.bannerViewPager.adapter = BannerAdapter(
            listOf(
                BannerItem(
                    title = "AI 规划你的职业路线",
                    subtitle = "结合兴趣测评和技能路径，生成可演示报告",
                    backgroundColor = Color.parseColor("#061947")
                ),
                BannerItem(
                    title = "先 Mock，稳定答辩",
                    subtitle = "核心报告离线生成，真实大模型 API 作为可选增强",
                    backgroundColor = Color.parseColor("#1E4ED8")
                ),
                BannerItem(
                    title = "课程知识点清晰覆盖",
                    subtitle = "Activity、Fragment、Room、RecyclerView、ViewPager2 一页可讲",
                    backgroundColor = Color.parseColor("#E0007A")
                )
            )
        )
    }

    private fun loadLatestAssessment() {
        val username = sessionManager.getUsername()
        if (username.isBlank()) {
            binding.tvLatestAssessment.text = "暂未登录，完成登录后可查看最近测评结果。"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val latest = withContext(Dispatchers.IO) {
                assessmentRepository.getResults(username).firstOrNull()
            }
            binding.tvLatestAssessment.text = if (latest == null) {
                "暂无测评结果。完成 MBTI 或霍兰德测试后，这里会展示最新记录。"
            } else {
                "${latest.type}：${latest.result}\n${latest.scoreDetail}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
