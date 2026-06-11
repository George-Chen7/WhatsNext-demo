package com.example.whatsnextdemo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.ai.AiCareerRepository
import com.example.whatsnextdemo.data.ai.RemoteAiCareerService
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.databinding.ActivityAiAnalyzingBinding
import com.example.whatsnextdemo.ui.ai.AiAnalysisUiState
import com.example.whatsnextdemo.ui.ai.AiAnalysisViewModel
import com.example.whatsnextdemo.ui.ai.AiReportActivity
import com.example.whatsnextdemo.utils.applySystemBarPadding
import kotlinx.coroutines.launch

class AiAnalyzingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiAnalyzingBinding

    private val viewModel: AiAnalysisViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        AiAnalysisViewModel.Factory(
            sessionManager = SessionManager(this),
            userRepository = UserRepository(database.userDao()),
            assessmentRepository = AssessmentRepository(database.assessmentResultDao()),
            careerReportRepository = CareerReportRepository(database.careerReportDao()),
            aiCareerRepository = AiCareerRepository(RemoteAiCareerService())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAnalyzingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        binding.btnRetryAi.setOnClickListener {
            viewModel.generateAnalysis()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                renderState(state)
            }
        }

        if (savedInstanceState == null) {
            viewModel.generateAnalysis()
        }
    }

    private fun renderState(state: AiAnalysisUiState) {
        when (state) {
            AiAnalysisUiState.Idle -> Unit
            AiAnalysisUiState.Loading -> {
                binding.progressAnalyzing.show()
                binding.btnRetryAi.visibility = View.GONE
                binding.tvAnalyzingTitle.text = "AI 正在分析"
                binding.tvAnalyzingMessage.text = "正在整合用户画像和五类测评结果..."
            }
            is AiAnalysisUiState.Success -> {
                binding.progressAnalyzing.hide()
                binding.btnRetryAi.visibility = View.GONE
                binding.tvAnalyzingTitle.text = "AI 分析完成"
                binding.tvAnalyzingMessage.text = "已生成结构化职业分析报告"
                val intent = Intent(this, AiReportActivity::class.java)
                    .putExtra(AiReportActivity.EXTRA_AI_REPORT_JSON, state.response.toJsonString())
                startActivity(intent)
                finish()
            }
            is AiAnalysisUiState.Error -> {
                binding.progressAnalyzing.hide()
                binding.btnRetryAi.visibility = View.VISIBLE
                binding.tvAnalyzingTitle.text = "AI 分析失败"
                binding.tvAnalyzingMessage.text = state.message
            }
        }
    }
}
