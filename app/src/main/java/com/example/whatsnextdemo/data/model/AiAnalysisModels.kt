package com.example.whatsnextdemo.data.model

import org.json.JSONArray
import org.json.JSONObject

data class MbtiResult(
    val type: String,
    val dimensionScores: Map<String, Int>
)

data class HollandResult(
    val topCode: String,
    val dimensionScores: Map<String, Int>
)

data class AiAnalysisRequest(
    val userProfile: UserProfile,
    val mbtiResult: MbtiResult,
    val hollandResult: HollandResult,
    val supplement: String = userProfile.extraNotes
)

data class ActionPlan(
    val shortTerm: String,
    val midTerm: String,
    val longTerm: String
)

data class AiAnalysisResponse(
    val summary: String,
    val personalityStrengths: List<String>,
    val suitableIndustries: List<String>,
    val suitablePositions: List<String>,
    val learningSuggestions: List<String>,
    val actionPlan: ActionPlan,
    val risks: List<String>,
    val finalAdvice: String
) {
    fun toJsonString(): String {
        return JSONObject()
            .put("summary", summary)
            .put("personalityStrengths", personalityStrengths.toJsonArray())
            .put("suitableIndustries", suitableIndustries.toJsonArray())
            .put("suitablePositions", suitablePositions.toJsonArray())
            .put("learningSuggestions", learningSuggestions.toJsonArray())
            .put(
                "actionPlan",
                JSONObject()
                    .put("shortTerm", actionPlan.shortTerm)
                    .put("midTerm", actionPlan.midTerm)
                    .put("longTerm", actionPlan.longTerm)
            )
            .put("risks", risks.toJsonArray())
            .put("finalAdvice", finalAdvice)
            .toString()
    }

    fun toDisplayText(): String {
        return buildString {
            appendLine("总体职业倾向分析")
            appendLine(summary)
            appendLine()
            appendSection("性格优势分析", personalityStrengths)
            appendSection("适合行业推荐", suitableIndustries)
            appendSection("适合岗位推荐", suitablePositions)
            appendSection("学习提升建议", learningSuggestions)
            appendLine("未来 1-3 年行动计划")
            appendLine("3 个月：${actionPlan.shortTerm}")
            appendLine("6-12 个月：${actionPlan.midTerm}")
            appendLine("1-3 年：${actionPlan.longTerm}")
            appendLine()
            appendSection("风险提醒", risks)
            appendLine("总结建议")
            appendLine(finalAdvice)
        }
    }

    private fun StringBuilder.appendSection(title: String, items: List<String>) {
        appendLine(title)
        items.forEachIndexed { index, item -> appendLine("${index + 1}. $item") }
        appendLine()
    }

    companion object {
        fun fromJsonString(json: String): AiAnalysisResponse {
            val root: JSONObject = JSONObject(json)
            val actionPlan: JSONObject = root.getJSONObject("actionPlan")
            return AiAnalysisResponse(
                summary = requireNonBlank(root.getString("summary"), "summary"),
                personalityStrengths = root.getJSONArray("personalityStrengths").toStringList("personalityStrengths"),
                suitableIndustries = root.getJSONArray("suitableIndustries").toStringList("suitableIndustries"),
                suitablePositions = root.getJSONArray("suitablePositions").toStringList("suitablePositions"),
                learningSuggestions = root.getJSONArray("learningSuggestions").toStringList("learningSuggestions"),
                actionPlan = ActionPlan(
                    shortTerm = requireNonBlank(actionPlan.getString("shortTerm"), "actionPlan.shortTerm"),
                    midTerm = requireNonBlank(actionPlan.getString("midTerm"), "actionPlan.midTerm"),
                    longTerm = requireNonBlank(actionPlan.getString("longTerm"), "actionPlan.longTerm")
                ),
                risks = root.getJSONArray("risks").toStringList("risks"),
                finalAdvice = requireNonBlank(root.getString("finalAdvice"), "finalAdvice")
            )
        }

        private fun requireNonBlank(value: String, fieldName: String): String {
            if (value.isBlank()) {
                throw org.json.JSONException("AI response field is blank: $fieldName")
            }
            return value
        }
    }
}

private fun List<String>.toJsonArray(): JSONArray {
    val array = JSONArray()
    forEach { array.put(it) }
    return array
}

private fun JSONArray.toStringList(fieldName: String): List<String> {
    if (length() == 0) {
        throw org.json.JSONException("AI response array is empty: $fieldName")
    }
    return List(length()) { index: Int ->
        val value: String = getString(index)
        if (value.isBlank()) {
            throw org.json.JSONException("AI response array item is blank: $fieldName[$index]")
        }
        value
    }
}
