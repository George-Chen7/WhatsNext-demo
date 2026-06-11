package com.example.whatsnextdemo.ui.ai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsnextdemo.MainActivity
import com.example.whatsnextdemo.data.model.AiAnalysisResponse
import com.example.whatsnextdemo.databinding.ActivityAiReportBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding

class AiReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        val response = AiAnalysisResponse.fromJsonString(
            intent.getStringExtra(EXTRA_AI_REPORT_JSON).orEmpty()
        )
        renderReport(response)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnDone.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_START_TAB, MainActivity.TAB_REPORT)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun renderReport(response: AiAnalysisResponse) {
        setCard(binding.cardSummary.root, "总体职业倾向分析", response.summary)
        setCard(binding.cardStrengths.root, "性格优势分析", response.personalityStrengths.toBullets())
        setCard(binding.cardIndustries.root, "适合行业推荐", response.suitableIndustries.toBullets())
        setCard(binding.cardPositions.root, "适合岗位推荐", response.suitablePositions.toBullets())
        setCard(binding.cardLearning.root, "学习提升建议", response.learningSuggestions.toBullets())
        setCard(
            binding.cardPlan.root,
            "未来 1-3 年行动计划",
            listOf(
                "未来 3 个月：${response.actionPlan.shortTerm}",
                "未来 6-12 个月：${response.actionPlan.midTerm}",
                "未来 1-3 年：${response.actionPlan.longTerm}"
            ).toBullets()
        )
        setCard(binding.cardRisks.root, "风险提醒", response.risks.toBullets())
        setCard(binding.cardAdvice.root, "总结建议", response.finalAdvice)
    }

    private fun setCard(cardRoot: android.view.View, title: String, content: String) {
        cardRoot.findViewById<TextView>(com.example.whatsnextdemo.R.id.tvCardTitle).text = title
        cardRoot.findViewById<TextView>(com.example.whatsnextdemo.R.id.tvCardContent).text = content
    }

    private fun List<String>.toBullets(): String {
        return joinToString(separator = "\n") { "• $it" }
    }

    companion object {
        const val EXTRA_AI_REPORT_JSON = "extra_ai_report_json"
    }
}
