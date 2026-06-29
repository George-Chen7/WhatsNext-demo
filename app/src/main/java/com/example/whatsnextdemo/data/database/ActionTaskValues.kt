package com.example.whatsnextdemo.data.database

object ActionTaskValues {
    const val PRIORITY_HIGH: String = "HIGH"
    const val PRIORITY_MEDIUM: String = "MEDIUM"
    const val PRIORITY_LOW: String = "LOW"

    const val SOURCE_MANUAL: String = "MANUAL"
    const val SOURCE_AI_REPORT: String = "AI_REPORT"

    const val CATEGORY_LEARNING: String = "学习提升"
    const val CATEGORY_JOB: String = "求职准备"
    const val CATEGORY_PROJECT: String = "项目实践"
    const val CATEGORY_EXPLORE: String = "职业探索"
    const val CATEGORY_OTHER: String = "其他"

    fun priorityLabel(priority: String): String {
        return when (priority) {
            PRIORITY_HIGH -> "高"
            PRIORITY_LOW -> "低"
            else -> "中"
        }
    }

    fun sourceLabel(source: String): String {
        return when (source) {
            SOURCE_AI_REPORT -> "AI 建议"
            else -> "手动添加"
        }
    }
}
