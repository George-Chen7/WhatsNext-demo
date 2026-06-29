package com.example.whatsnextdemo.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.MainActivity
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.ActionTaskRepository
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.FragmentHomeBinding
import com.example.whatsnextdemo.ui.assessment.AssessmentScorer
import com.example.whatsnextdemo.ui.onboarding.AiAnalyzingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var assessmentRepository: AssessmentRepository
    private lateinit var actionTaskRepository: ActionTaskRepository
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val database: AppDatabase = AppDatabase.getInstance(requireContext())
        assessmentRepository = AssessmentRepository(database.assessmentResultDao())
        actionTaskRepository = ActionTaskRepository(database.actionTaskDao())

        setupHeader()
        setupActions()
        loadLatestAssessment()
        loadActionPlanSummary()
    }

    override fun onResume(): Unit {
        super.onResume()
        if (this::actionTaskRepository.isInitialized) {
            loadActionPlanSummary()
        }
    }

    private fun setupHeader(): Unit {
        val username: String = sessionManager.getUsername().ifBlank { "同学" }
        binding.tvWelcome.text = "你好，$username"
        binding.tvAvatar.text = username.take(1).uppercase()
        binding.tvAssistantMessage.text = "我会先了解你的兴趣、性格和能力，再把结果整理成一份适合答辩展示的职业规划报告。"
    }

    private fun setupActions(): Unit {
        binding.btnStartAssessment.setOnClickListener {
            requireMainActivity().openAssessmentTab()
        }
        binding.btnGenerateReport.setOnClickListener {
            val intent: Intent = Intent(requireContext(), AiAnalyzingActivity::class.java)
            startActivity(intent)
        }
        binding.btnViewReport.setOnClickListener {
            requireMainActivity().openReportTab()
        }
        binding.btnViewActionPlan.setOnClickListener {
            requireMainActivity().openActionPlanTab()
        }
        binding.btnCreateHomeTask.setOnClickListener {
            requireMainActivity().openActionPlanTab()
        }
    }

    private fun loadLatestAssessment(): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            binding.tvLatestAssessment.text = "AI 助手：暂未登录，完成登录后可查看最近测评结果。"
            updateReportEntry(isEnabled = false)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val results: List<AssessmentResultEntity> = withContext(Dispatchers.IO) {
                assessmentRepository.getResults(username)
            }
            val latest: AssessmentResultEntity? = results.firstOrNull()
            val hasRequiredResults: Boolean = hasRequiredAssessmentResults(results)

            binding.tvLatestAssessment.text = if (latest == null) {
                "AI 助手：还没有测评结果。先完成 MBTI 和霍兰德测试，我就能生成职业路线建议。"
            } else {
                "AI 助手：最近一次测评是 ${latest.type}，结果为 ${latest.result}。\n${latest.scoreDetail}"
            }
            updateReportEntry(hasRequiredResults)
        }
    }

    private fun hasRequiredAssessmentResults(results: List<AssessmentResultEntity>): Boolean {
        val hasHolland: Boolean = results.any { it.type == AssessmentScorer.TYPE_HOLLAND }
        val hasMbti: Boolean = results.any { it.type == AssessmentScorer.TYPE_MBTI }
        return hasHolland && hasMbti
    }

    private fun updateReportEntry(isEnabled: Boolean): Unit {
        binding.btnGenerateReport.isEnabled = isEnabled
        binding.btnGenerateReport.alpha = if (isEnabled) 1.0f else 0.48f
        binding.btnGenerateReport.text = if (isEnabled) {
            "生成 AI 职业报告"
        } else {
            "完成 MBTI 和霍兰德后生成报告"
        }
    }

    private fun loadActionPlanSummary(): Unit {
        val username: String = sessionManager.getUsername()
        if (username.isBlank()) {
            binding.tvActionPlanStats.text = "暂未登录"
            binding.tvUpcomingTasks.text = "登录后可以查看自己的行动计划。"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val summary: HomeActionPlanSummary = withContext(Dispatchers.IO) {
                val totalCount: Int = actionTaskRepository.countTasks(username)
                val completedCount: Int = actionTaskRepository.countCompletedTasks(username)
                val upcomingTasks: List<ActionTaskEntity> = actionTaskRepository.getUpcomingTasks(username, 3)
                HomeActionPlanSummary(totalCount, completedCount, upcomingTasks)
            }
            renderActionPlanSummary(summary)
        }
    }

    private fun renderActionPlanSummary(summary: HomeActionPlanSummary): Unit {
        val activeCount: Int = summary.totalCount - summary.completedCount
        binding.tvActionPlanStats.text =
            "进行中 $activeCount · 已完成 ${summary.completedCount} · 总任务 ${summary.totalCount}"
        binding.tvUpcomingTasks.text = if (summary.totalCount == 0) {
            "暂无行动任务。\n可以手动新建，或从职业报告中导入建议。"
        } else if (summary.upcomingTasks.isEmpty()) {
            "当前没有进行中的任务。"
        } else {
            summary.upcomingTasks.joinToString(separator = "\n") { task ->
                val dueText: String = task.dueDate?.let { dueDate ->
                    "截止 ${dateFormatter.format(Date(dueDate))}"
                } ?: "无截止日期"
                "• ${task.title}（$dueText）"
            }
        }
    }

    private fun requireMainActivity(): MainActivity {
        val hostActivity = requireActivity()
        if (hostActivity !is MainActivity) {
            throw IllegalStateException("HomeFragment must be hosted by MainActivity.")
        }
        return hostActivity
    }

    override fun onDestroyView(): Unit {
        super.onDestroyView()
        _binding = null
    }
}

private data class HomeActionPlanSummary(
    val totalCount: Int,
    val completedCount: Int,
    val upcomingTasks: List<ActionTaskEntity>
)
