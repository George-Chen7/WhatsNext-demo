package com.example.whatsnextdemo.ui.assessment

import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import org.json.JSONObject

object ManualAssessmentResultFactory {
    fun buildResult(
        username: String,
        assessmentType: String,
        rawResult: String,
        createTime: Long
    ): AssessmentResultEntity {
        val normalizedResult: String = rawResult.trim().uppercase()
        if (normalizedResult.isBlank()) {
            throw IllegalArgumentException("测试结果不能为空")
        }
        val scoreDetail: String = when (assessmentType) {
            AssessmentScorer.TYPE_HOLLAND -> buildHollandScoreDetail(normalizedResult)
            AssessmentScorer.TYPE_MBTI -> buildMbtiScoreDetail(normalizedResult)
            else -> throw IllegalArgumentException("不支持的测评类型：$assessmentType")
        }
        return AssessmentResultEntity(
            id = 0,
            username = username,
            type = assessmentType,
            result = normalizedResult,
            scoreDetail = scoreDetail,
            createTime = createTime
        )
    }

    private fun buildHollandScoreDetail(result: String): String {
        val dimensions: List<String> = listOf("R", "I", "A", "S", "E", "C")
        val resultLetters: List<String> = result.map { letter: Char -> letter.toString() }
        if (resultLetters.size != 3 || resultLetters.toSet().size != 3) {
            throw IllegalArgumentException("霍兰德结果请输入 3 个不重复字母，例如 RIA")
        }
        val invalidLetters: List<String> = resultLetters.filter { letter: String -> letter !in dimensions }
        if (invalidLetters.isNotEmpty()) {
            throw IllegalArgumentException("霍兰德结果只能包含 R、I、A、S、E、C")
        }

        val scores: MutableMap<String, Int> = dimensions.associateWith { 0 }.toMutableMap()
        resultLetters.forEachIndexed { index: Int, letter: String ->
            scores[letter] = resultLetters.size - index
        }
        return scores.toJsonString(dimensions)
    }

    private fun buildMbtiScoreDetail(result: String): String {
        val pattern: Regex = Regex("^[EI][SN][TF][JP]$")
        if (!pattern.matches(result)) {
            throw IllegalArgumentException("MBTI 结果请输入 4 个字母，例如 INTJ")
        }

        val dimensions: List<String> = listOf("E", "I", "S", "N", "T", "F", "J", "P")
        val scores: MutableMap<String, Int> = dimensions.associateWith { 0 }.toMutableMap()
        result.forEach { letter: Char ->
            scores[letter.toString()] = 1
        }
        return scores.toJsonString(dimensions)
    }

    private fun Map<String, Int>.toJsonString(order: List<String>): String {
        val json = JSONObject()
        order.forEach { key: String -> json.put(key, getValue(key)) }
        return json.toString()
    }
}
