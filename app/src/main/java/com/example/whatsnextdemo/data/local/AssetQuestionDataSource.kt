package com.example.whatsnextdemo.data.local

import android.content.Context
import com.example.whatsnextdemo.data.model.AssessmentQuestion
import org.json.JSONArray
import org.json.JSONObject

class AssetQuestionDataSource(private val context: Context) {
    fun loadHollandQuestions(): List<AssessmentQuestion> {
        val json = readAsset("holland_questions.json")
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            AssessmentQuestion(
                id = item.getInt("id"),
                dimension = item.getString("dimension"),
                dimensionName = item.optString("dimensionName"),
                question = item.getString("question"),
                reverse = item.optBoolean("reverse", false),
                options = LIKERT_OPTIONS,
                optionTypes = emptyList()
            )
        }
    }

    fun loadMbtiQuestions(): List<AssessmentQuestion> {
        val json = runCatching { readAsset("mbti_questions.json") }
            .getOrElse { readAsset("mbti-questios.json") }
        val array = JSONObject(json).getJSONArray("questions")
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val types = item.getJSONArray("types").toStringList()
            AssessmentQuestion(
                id = item.getInt("id"),
                dimension = item.getString("dimension"),
                dimensionName = item.getString("dimension"),
                question = item.getString("title"),
                options = item.getJSONArray("selections").toStringList(),
                optionTypes = types
            )
        }
    }

    fun loadCareerAbilityQuestions(): List<AssessmentQuestion> {
        return loadLikertQuestions(
            fileName = "career_ability_questions.json",
            dimensionNames = CAREER_ABILITY_DIMENSION_NAMES
        )
    }

    fun loadCareerAnchorQuestions(): List<AssessmentQuestion> {
        return loadLikertQuestions(
            fileName = "career_anchor_questions.json",
            dimensionNames = CAREER_ANCHOR_DIMENSION_NAMES
        )
    }

    fun loadCareerValuesQuestions(): List<AssessmentQuestion> {
        return loadLikertQuestions(
            fileName = "career_values_questions.json",
            dimensionNames = CAREER_VALUES_DIMENSION_NAMES
        )
    }

    private fun loadLikertQuestions(
        fileName: String,
        dimensionNames: Map<String, String>
    ): List<AssessmentQuestion> {
        val json = readAsset(fileName)
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val dimension = item.getString("dimension")
            AssessmentQuestion(
                id = item.getInt("id"),
                dimension = dimension,
                dimensionName = dimensionNames[dimension] ?: dimension,
                question = item.getString("question"),
                reverse = item.optBoolean("reverse", false),
                options = LIKERT_OPTIONS,
                optionTypes = emptyList()
            )
        }
    }

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun JSONArray.toStringList(): List<String> {
        return List(length()) { index -> getString(index) }
    }

    companion object {
        val LIKERT_OPTIONS: List<String> = listOf("非常不同意", "不同意", "一般", "同意", "非常同意")

        private val CAREER_ABILITY_DIMENSION_NAMES: Map<String, String> = mapOf(
            "logic" to "逻辑分析",
            "communication" to "沟通表达",
            "math" to "数据数学",
            "creativity" to "创新创造",
            "organization" to "组织协作",
            "execution" to "执行落地"
        )

        private val CAREER_ANCHOR_DIMENSION_NAMES: Map<String, String> = mapOf(
            "technical" to "技术职能型",
            "management" to "管理型",
            "entrepreneurial" to "创业型",
            "autonomy" to "自主独立型",
            "security" to "安全稳定型",
            "service" to "服务奉献型",
            "challenge" to "挑战型"
        )

        private val CAREER_VALUES_DIMENSION_NAMES: Map<String, String> = mapOf(
            "high_income" to "高收入",
            "stability" to "稳定保障",
            "social_contribution" to "社会贡献",
            "work_life_balance" to "工作生活平衡",
            "innovation" to "创新成长",
            "leadership" to "领导影响"
        )
    }
}
