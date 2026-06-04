package com.example.whatsnextdemo.ui.assessment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.local.AssetQuestionDataSource
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.AssessmentQuestion
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.databinding.ActivityQuestionBinding
import com.example.whatsnextdemo.databinding.DialogAssessmentExitBinding
import com.example.whatsnextdemo.databinding.DialogAssessmentResultBinding
import com.example.whatsnextdemo.utils.applySystemBarPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private lateinit var questions: List<AssessmentQuestion>
    private lateinit var assessmentRepository: AssessmentRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var optionButtons: List<RadioButton>
    private val answers = mutableMapOf<Int, Int>()
    private var assessmentType: String = AssessmentScorer.TYPE_HOLLAND
    private var currentQuestionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarPadding()

        assessmentType = intent.getStringExtra(AssessmentScorer.EXTRA_ASSESSMENT_TYPE)
            ?: AssessmentScorer.TYPE_HOLLAND
        sessionManager = SessionManager(this)
        assessmentRepository = AssessmentRepository(
            AppDatabase.getInstance(this).assessmentResultDao()
        )
        optionButtons = listOf(
            binding.rbOption1,
            binding.rbOption2,
            binding.rbOption3,
            binding.rbOption4,
            binding.rbOption5
        )

        setupQuestions()
        setupActions()
    }

    private fun setupQuestions() {
        val dataSource = AssetQuestionDataSource(this)
        questions = when (assessmentType) {
            AssessmentScorer.TYPE_MBTI -> dataSource.loadMbtiQuestions()
            else -> dataSource.loadHollandQuestions()
        }

        binding.tvQuestionTitle.text = when (assessmentType) {
            AssessmentScorer.TYPE_MBTI -> "MBTI 简化测试"
            else -> "霍兰德职业兴趣测试"
        }
        renderCurrentQuestion()
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            confirmExit()
        }

        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                renderCurrentQuestion()
            }
        }

        binding.btnNext.setOnClickListener {
            if (!hasAnsweredCurrentQuestion()) {
                Toast.makeText(this, "请选择当前题目的答案", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentQuestionIndex < questions.lastIndex) {
                currentQuestionIndex++
                renderCurrentQuestion()
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (!hasAnsweredCurrentQuestion()) {
                Toast.makeText(this, "请选择当前题目的答案", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (answers.size < questions.size) {
                val firstUnansweredIndex = questions.indexOfFirst { answers[it.id] == null }
                currentQuestionIndex = firstUnansweredIndex.coerceAtLeast(0)
                renderCurrentQuestion()
                Toast.makeText(
                    this,
                    "还有 ${questions.size - answers.size} 道题未完成，请继续作答",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val scoreResult = AssessmentScorer.score(assessmentType, questions, answers.toMap())
            saveAndShowResult(scoreResult)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmExit()
                }
            }
        )
    }

    private fun renderCurrentQuestion() {
        val question = questions[currentQuestionIndex]
        binding.tvProgress.text =
            "第 ${currentQuestionIndex + 1} / ${questions.size} 题，已完成 ${answers.size} / ${questions.size}"
        binding.tvDimension.text = question.dimensionName.ifBlank { question.dimension }
        binding.tvCurrentQuestion.text =
            "${question.id}. ${question.question.removePrefix("${question.id}. ")}"

        binding.radioGroup.setOnCheckedChangeListener(null)
        optionButtons.forEachIndexed { index, radioButton ->
            val option = question.options.getOrNull(index)
            radioButton.visibility = if (option == null) View.GONE else View.VISIBLE
            radioButton.text = option.orEmpty()
        }

        val checkedIndex = answers[question.id] ?: -1
        if (checkedIndex == -1) {
            binding.radioGroup.clearCheck()
        } else {
            binding.radioGroup.check(optionButtons[checkedIndex].id)
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedIndex = optionButtons.indexOfFirst { it.id == checkedId }
            if (selectedIndex >= 0) {
                answers[question.id] = selectedIndex
                if (currentQuestionIndex < questions.lastIndex) {
                    currentQuestionIndex++
                    renderCurrentQuestion()
                } else {
                    updateProgressOnly()
                }
            }
        }

        binding.btnPrevious.isEnabled = currentQuestionIndex > 0
        binding.btnNext.visibility =
            if (currentQuestionIndex == questions.lastIndex) View.GONE else View.VISIBLE
        binding.btnSubmit.visibility =
            if (currentQuestionIndex == questions.lastIndex) View.VISIBLE else View.GONE
    }

    private fun updateProgressOnly() {
        binding.tvProgress.text =
            "第 ${currentQuestionIndex + 1} / ${questions.size} 题，已完成 ${answers.size} / ${questions.size}"
    }

    private fun hasAnsweredCurrentQuestion(): Boolean {
        return answers[questions[currentQuestionIndex].id] != null
    }

    private fun confirmExit() {
        if (answers.isEmpty()) {
            finish()
            return
        }
        val dialogBinding = DialogAssessmentExitBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialogBinding.tvExitMessage.text =
            "你已经完成 ${answers.size} / ${questions.size} 道题。\n当前版本没有答题暂存，离开后本次未提交的答案不会保存。"
        dialogBinding.btnContinue.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnExit.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

    private fun saveAndShowResult(scoreResult: ScoreResult) {
        binding.btnSubmit.isEnabled = false
        lifecycleScope.launch {
            val username = sessionManager.getUsername().ifBlank { "guest" }
            withContext(Dispatchers.IO) {
                assessmentRepository.saveResult(
                    AssessmentResultEntity(
                        username = username,
                        type = assessmentType,
                        result = scoreResult.result,
                        scoreDetail = scoreResult.scoreDetail
                    )
                )
            }
            binding.btnSubmit.isEnabled = true
            showResultDialog(scoreResult)
        }
    }

    private fun showResultDialog(scoreResult: ScoreResult) {
        val dialogBinding = DialogAssessmentResultBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialogBinding.tvResult.text = scoreResult.result
        dialogBinding.tvScoreDetail.text = formatScoreDetail(scoreResult.scoreDetail)
        dialogBinding.btnStay.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnReturn.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

    private fun formatScoreDetail(scoreDetail: String): String {
        val json = JSONObject(scoreDetail)
        return json.keys().asSequence()
            .joinToString(separator = "    ") { key -> "$key ${json.getInt(key)}" }
            .let { "分数明细\n$it" }
    }
}
