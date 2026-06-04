package com.example.whatsnextdemo.ui.assessment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsnextdemo.data.model.AssessmentQuestion
import com.example.whatsnextdemo.databinding.ItemAssessmentQuestionBinding

class QuestionAdapter(
    private val questions: List<AssessmentQuestion>,
    private val onAnswerChanged: (questionId: Int, selectedIndex: Int) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private val answers = mutableMapOf<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemAssessmentQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int = questions.size

    fun getAnswers(): Map<Int, Int> = answers.toMap()

    inner class QuestionViewHolder(
        private val binding: ItemAssessmentQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val optionButtons: List<RadioButton> = listOf(
            binding.rbOption1,
            binding.rbOption2,
            binding.rbOption3,
            binding.rbOption4,
            binding.rbOption5
        )

        fun bind(question: AssessmentQuestion) {
            binding.tvQuestionTitle.text = "${question.id}. ${question.question.removePrefix("${question.id}. ")}"
            binding.tvDimension.text = question.dimensionName.ifBlank { question.dimension }
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
                    onAnswerChanged(question.id, selectedIndex)
                }
            }
        }
    }
}
