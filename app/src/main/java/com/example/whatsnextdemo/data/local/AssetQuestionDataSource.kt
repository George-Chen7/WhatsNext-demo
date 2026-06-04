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
                options = HOLLAND_OPTIONS
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

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun JSONArray.toStringList(): List<String> {
        return List(length()) { index -> getString(index) }
    }

    companion object {
        val HOLLAND_OPTIONS = listOf("非常不同意", "不同意", "一般", "同意", "非常同意")
    }
}
