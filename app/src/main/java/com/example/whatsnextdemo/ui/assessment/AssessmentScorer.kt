package com.example.whatsnextdemo.ui.assessment

import com.example.whatsnextdemo.data.model.AssessmentQuestion
import org.json.JSONObject

object AssessmentScorer {
    const val TYPE_HOLLAND = "HOLLAND"
    const val TYPE_MBTI = "MBTI"
    const val TYPE_CAREER_ABILITY = "CAREER_ABILITY"
    const val TYPE_CAREER_ANCHOR = "CAREER_ANCHOR"
    const val TYPE_CAREER_VALUES = "CAREER_VALUES"
    const val EXTRA_ASSESSMENT_TYPE = "assessment_type"

    fun score(type: String, questions: List<AssessmentQuestion>, answers: Map<Int, Int>): ScoreResult {
        return when (type) {
            TYPE_HOLLAND -> scoreHolland(questions, answers)
            TYPE_MBTI -> scoreMbti(questions, answers)
            TYPE_CAREER_ABILITY -> scoreLikertScale(
                questions = questions,
                answers = answers,
                dimensionOrder = CAREER_ABILITY_ORDER
            )
            TYPE_CAREER_ANCHOR -> scoreLikertScale(
                questions = questions,
                answers = answers,
                dimensionOrder = CAREER_ANCHOR_ORDER
            )
            TYPE_CAREER_VALUES -> scoreLikertScale(
                questions = questions,
                answers = answers,
                dimensionOrder = CAREER_VALUES_ORDER
            )
            else -> error("Unsupported assessment type: $type")
        }
    }

    fun titleOf(type: String): String {
        return when (type) {
            TYPE_HOLLAND -> "霍兰德职业兴趣测试"
            TYPE_MBTI -> "MBTI 简化测试"
            TYPE_CAREER_ABILITY -> "职业能力测评"
            TYPE_CAREER_ANCHOR -> "职业锚测评"
            TYPE_CAREER_VALUES -> "职业价值观测评"
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

    private fun scoreLikertScale(
        questions: List<AssessmentQuestion>,
        answers: Map<Int, Int>,
        dimensionOrder: List<String>
    ): ScoreResult {
        val scores = dimensionOrder.associateWith { 0 }.toMutableMap()
        val dimensionNames = questions.associate { question ->
            question.dimension to question.dimensionName.ifBlank { question.dimension }
        }
        questions.forEach { question ->
            val selectedIndex = answers.getValue(question.id)
            val rawScore = selectedIndex + 1
            val score = if (question.reverse) 6 - rawScore else rawScore
            scores[question.dimension] = scores.getValue(question.dimension) + score
        }
        val result = scores.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { dimensionOrder.indexOf(it.key) })
            .take(3)
            .joinToString("、") { entry -> dimensionNames.getValue(entry.key) }
        return ScoreResult(result, scores.toNamedJsonString(dimensionOrder, dimensionNames))
    }

    private fun Map<String, Int>.toJsonString(order: List<String>): String {
        val json = JSONObject()
        order.forEach { key -> json.put(key, getValue(key)) }
        return json.toString()
    }

    private fun Map<String, Int>.toNamedJsonString(
        order: List<String>,
        dimensionNames: Map<String, String>
    ): String {
        val json = JSONObject()
        order.forEach { key -> json.put(dimensionNames.getValue(key), getValue(key)) }
        return json.toString()
    }

    private val CAREER_ABILITY_ORDER: List<String> = listOf(
        "logic",
        "communication",
        "math",
        "creativity",
        "organization",
        "execution"
    )

    private val CAREER_ANCHOR_ORDER: List<String> = listOf(
        "technical",
        "management",
        "entrepreneurial",
        "autonomy",
        "security",
        "service",
        "challenge"
    )

    private val CAREER_VALUES_ORDER: List<String> = listOf(
        "high_income",
        "stability",
        "social_contribution",
        "work_life_balance",
        "innovation",
        "leadership"
    )
}

data class ScoreResult(
    val result: String,
    val scoreDetail: String
)
