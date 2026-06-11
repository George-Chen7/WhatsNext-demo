package com.example.whatsnextdemo.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.ActivityAssessmentGuideBinding
import com.example.whatsnextdemo.ui.assessment.AssessmentScorer
import com.example.whatsnextdemo.ui.assessment.QuestionActivity
import com.example.whatsnextdemo.utils.applySystemBarPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssessmentGuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssessmentGuideBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var assessmentRepository: AssessmentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        sessionManager = SessionManager(this)
        assessmentRepository = AssessmentRepository(
            AppDatabase.getInstance(this).assessmentResultDao()
        )

        binding.btnStartHolland.setOnClickListener {
            openQuestion(AssessmentScorer.TYPE_HOLLAND)
        }
        binding.btnStartMbti.setOnClickListener {
            openQuestion(AssessmentScorer.TYPE_MBTI)
        }
        binding.btnStartAi.setOnClickListener {
            startActivity(Intent(this, AiAnalyzingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun openQuestion(type: String) {
        val intent = Intent(this, QuestionActivity::class.java)
            .putExtra(AssessmentScorer.EXTRA_ASSESSMENT_TYPE, type)
        startActivity(intent)
    }

    private fun refreshStatus() {
        lifecycleScope.launch {
            val username = sessionManager.getUsername().ifBlank { "guest" }
            val results = withContext(Dispatchers.IO) {
                assessmentRepository.getResults(username)
            }
            val hasHolland = results.any { it.type == AssessmentScorer.TYPE_HOLLAND }
            val hasMbti = results.any { it.type == AssessmentScorer.TYPE_MBTI }

            binding.tvHollandStatus.text = if (hasHolland) "已完成" else "未完成"
            binding.tvMbtiStatus.text = if (hasMbti) "已完成" else "未完成"
            binding.btnStartHolland.text = if (hasHolland) "重新测评霍兰德" else "开始霍兰德测试"
            binding.btnStartMbti.text = if (hasMbti) "重新测评 MBTI" else "开始 MBTI 测试"
            binding.btnStartAi.isEnabled = hasHolland && hasMbti
            binding.tvAiHint.text = if (hasHolland && hasMbti) {
                sessionManager.setAssessmentCompleted()
                "测评已完成，可以生成 AI 职业分析报告"
            } else {
                "请先完成两项职业测评"
            }
        }
    }
}
