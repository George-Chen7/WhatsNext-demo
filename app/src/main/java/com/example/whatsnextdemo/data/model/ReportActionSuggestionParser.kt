package com.example.whatsnextdemo.data.model

object ReportActionSuggestionParser {
    private const val ACTION_PLAN_TITLE: String = "未来 1-3 年行动计划"
    private val nextSectionTitles: Set<String> = setOf(
        "风险提醒",
        "总结建议",
        "总体职业倾向分析",
        "性格优势分析",
        "适合行业推荐",
        "适合岗位推荐",
        "学习提升建议"
    )
    private val periodPattern: Regex = Regex("^(.+?)[:：]\\s*(.+)$")

    fun parse(reportContent: String): List<ReportActionSuggestion> {
        val cleanedContent: String = reportContent.trim()
        if (cleanedContent.isBlank()) {
            return emptyList()
        }

        val planLines: List<String> = extractActionPlanLines(cleanedContent)
        val suggestions: List<ReportActionSuggestion> = planLines.mapNotNull { line ->
            parseLine(line)
        }
        if (suggestions.isNotEmpty()) {
            return suggestions
        }
        return listOf(ReportActionSuggestion("职业报告建议", cleanedContent))
    }

    private fun extractActionPlanLines(reportContent: String): List<String> {
        val lines: List<String> = reportContent.lines()
        val titleIndex: Int = lines.indexOfFirst { line ->
            line.trim() == ACTION_PLAN_TITLE
        }
        if (titleIndex < 0) {
            return emptyList()
        }

        val result: MutableList<String> = mutableListOf()
        for (index: Int in titleIndex + 1 until lines.size) {
            val line: String = lines[index].trim()
            if (line.isBlank()) {
                if (result.isNotEmpty()) {
                    break
                }
                continue
            }
            if (nextSectionTitles.contains(line)) {
                break
            }
            result.add(line.removePrefix("-").removePrefix("•").trim())
        }
        return result
    }

    private fun parseLine(line: String): ReportActionSuggestion? {
        val normalizedLine: String = line
            .replace(Regex("^\\d+[.、]\\s*"), "")
            .trim()
        if (normalizedLine.isBlank()) {
            return null
        }
        val match: MatchResult = periodPattern.find(normalizedLine)
            ?: return ReportActionSuggestion("职业报告建议", normalizedLine)
        val period: String = match.groupValues[1].trim()
        val title: String = match.groupValues[2].trim()
        if (title.isBlank()) {
            return null
        }
        return ReportActionSuggestion(period, title)
    }
}
