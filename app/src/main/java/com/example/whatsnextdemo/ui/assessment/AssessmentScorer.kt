package com.example.whatsnextdemo.ui.assessment

import com.example.whatsnextdemo.data.model.AssessmentQuestion
import org.json.JSONObject

object AssessmentScorer {
    const val TYPE_HOLLAND = "HOLLAND"
    const val TYPE_MBTI = "MBTI"
    const val EXTRA_ASSESSMENT_TYPE = "assessment_type"

    fun score(type: String, questions: List<AssessmentQuestion>, answers: Map<Int, Int>): ScoreResult {
        return when (type) {
            TYPE_HOLLAND -> scoreHolland(questions, answers)
            TYPE_MBTI -> scoreMbti(questions, answers)
            else -> error("Unsupported assessment type: $type")
        }
    }

    private fun scoreHolland(
        questions: List<AssessmentQuestion>,
        answers: Map<Int, Int>
    ): ScoreResult {
        val order = listOf("R", "I", "A", "S", "E", "C")
        val scores = order.associateWith { 0 }.toMutableMap()
        questions.forEach { question ->
            val selectedIndex = answers.getValue(question.id)
            val rawScore = selectedIndex + 1
            val score = if (question.reverse) 6 - rawScore else rawScore
            scores[question.dimension] = scores.getValue(question.dimension) + score
        }
        val result = scores.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { order.indexOf(it.key) })
            .take(3)
            .joinToString("") { it.key }
        return ScoreResult(result, scores.toJsonString(order))
    }

    private fun scoreMbti(
        questions: List<AssessmentQuestion>,
        answers: Map<Int, Int>
    ): ScoreResult {
        val order = listOf("E", "I", "S", "N", "T", "F", "J", "P")
        val scores = order.associateWith { 0 }.toMutableMap()
        questions.forEach { question ->
            val selectedIndex = answers.getValue(question.id)
            val type = question.optionTypes.getOrNull(selectedIndex).orEmpty()
            if (type.isNotBlank()) {
                scores[type] = scores.getValue(type) + 1
            }
        }
        val result = buildString {
            append(if (scores.getValue("E") >= scores.getValue("I")) "E" else "I")
            append(if (scores.getValue("S") >= scores.getValue("N")) "S" else "N")
            append(if (scores.getValue("T") >= scores.getValue("F")) "T" else "F")
            append(if (scores.getValue("J") >= scores.getValue("P")) "J" else "P")
        }
        return ScoreResult(result, scores.toJsonString(order))
    }

    private fun Map<String, Int>.toJsonString(order: List<String>): String {
        val json = JSONObject()
        order.forEach { key -> json.put(key, getValue(key)) }
        return json.toString()
    }
}

data class ScoreResult(
    val result: String,
    val scoreDetail: String
)
