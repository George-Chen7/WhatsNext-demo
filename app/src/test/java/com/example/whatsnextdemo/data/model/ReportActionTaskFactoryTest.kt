package com.example.whatsnextdemo.data.model

import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ReportActionTaskFactoryTest {
    @Test
    fun createTasksUsesCurrentUserAndNeutralDefaults(): Unit {
        val report: CareerReportEntity = CareerReportEntity(
            id = 42L,
            username = "alice",
            title = "AI 职业分析报告",
            createTime = 1000L,
            content = "未来 1-3 年行动计划",
            mbti = "INTJ",
            holland = "IRC"
        )
        val suggestions: List<ReportActionSuggestion> = listOf(
            ReportActionSuggestion("3 个月", "完成一版简历"),
            ReportActionSuggestion("6-12 个月", "投递实习")
        )

        val tasks: List<ActionTaskEntity> = ReportActionTaskFactory.createTasks(
            username = "alice",
            report = report,
            suggestions = suggestions,
            createTime = 2000L
        )

        assertEquals(2, tasks.size)
        assertEquals("alice", tasks[0].username)
        assertEquals("完成一版简历", tasks[0].title)
        assertEquals(ActionTaskValues.SOURCE_AI_REPORT, tasks[0].source)
        assertEquals(42L, tasks[0].sourceReportId)
        assertEquals(ActionTaskValues.PRIORITY_MEDIUM, tasks[0].priority)
        assertEquals(ActionTaskValues.CATEGORY_OTHER, tasks[0].category)
        assertNull(tasks[0].dueDate)
        assertFalse(tasks[0].isCompleted)
        assertNull(tasks[0].completeTime)
        assertEquals(2000L, tasks[0].createTime)
        assertEquals("来自报告：AI 职业分析报告\n周期：3 个月", tasks[0].note)
    }
}
