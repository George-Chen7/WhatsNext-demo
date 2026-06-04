package com.example.whatsnextdemo.service

import com.example.whatsnextdemo.data.model.UserProfile

class MockCareerAiService : CareerAiService {
    override suspend fun generateCareerReport(profile: UserProfile): String {
        return """
            # ${profile.nickname} 的职业规划报告

            ## 推荐方向
            - AI 产品经理
            - 数据分析师
            - Android 应用开发工程师

            ## 发展建议
            结合专业基础、职业兴趣和城市产业优势，优先补齐项目作品、数据分析和表达能力。
        """.trimIndent()
    }
}
