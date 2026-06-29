package com.example.whatsnextdemo.data.model

import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity

object ReportActionTaskFactory {
    fun createTasks(
        username: String,
        report: CareerReportEntity,
        suggestions: List<ReportActionSuggestion>,
        createTime: Long
    ): List<ActionTaskEntity> {
        return suggestions.map { suggestion ->
            ActionTaskEntity(
                id = 0,
                username = username,
                title = suggestion.title,
                note = "来自报告：${report.title}\n周期：${suggestion.period}",
                category = ActionTaskValues.CATEGORY_OTHER,
                priority = ActionTaskValues.PRIORITY_MEDIUM,
                dueDate = null,
                isCompleted = false,
                source = ActionTaskValues.SOURCE_AI_REPORT,
                sourceReportId = report.id,
                createTime = createTime,
                completeTime = null
            )
        }
    }
}
