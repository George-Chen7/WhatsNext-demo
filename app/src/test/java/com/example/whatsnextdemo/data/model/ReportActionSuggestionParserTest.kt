package com.example.whatsnextdemo.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportActionSuggestionParserTest {
    @Test
    fun parseSuggestionsUsesActionPlanSectionOnly(): Unit {
        val reportContent: String = """
            总体职业倾向分析
            适合从产品和数据方向切入。

            学习提升建议
            1. 补充 SQL 基础。

            未来 1-3 年行动计划
            3 个月：完成一版简历与课程设计项目说明。
            6-12 个月：选择 2-3 类岗位持续投递实习。
            1-3 年：根据实习反馈确定主方向。

            风险提醒
            1. 不要只根据单次测评决定方向。
        """.trimIndent()

        val suggestions: List<ReportActionSuggestion> =
            ReportActionSuggestionParser.parse(reportContent)

        assertEquals(
            listOf(
                ReportActionSuggestion("3 个月", "完成一版简历与课程设计项目说明。"),
                ReportActionSuggestion("6-12 个月", "选择 2-3 类岗位持续投递实习。"),
                ReportActionSuggestion("1-3 年", "根据实习反馈确定主方向。")
            ),
            suggestions
        )
    }

    @Test
    fun parseSuggestionsFallsBackToWholeContentWhenPlanSectionIsMissing(): Unit {
        val reportContent: String = "请先整理个人资料，再根据目标岗位补齐作品集。"

        val suggestions: List<ReportActionSuggestion> =
            ReportActionSuggestionParser.parse(reportContent)

        assertEquals(
            listOf(ReportActionSuggestion("职业报告建议", reportContent)),
            suggestions
        )
    }
}
