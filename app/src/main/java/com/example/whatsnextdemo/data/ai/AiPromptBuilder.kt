package com.example.whatsnextdemo.data.ai

import com.example.whatsnextdemo.data.model.AiAnalysisRequest

object AiPromptBuilder {
    fun buildCareerAnalysisPrompt(request: AiAnalysisRequest): String {
        val profile = request.userProfile
        return """
            你是一名面向大学生的职业规划顾问，请根据用户资料、MBTI 结果和霍兰德职业兴趣结果，生成客观、具体、可执行的职业规划分析。

            分析要求：
            1. 结合用户画像、专业背景、行业偏好和测评结果综合判断。
            2. 不要生成过度绝对化结论，不要把 MBTI 当成唯一判断依据。
            3. 建议要适合大学生课程设计 App 的职业规划场景，重点体现学习路径、岗位探索和 1-3 年行动计划。
            4. 只返回 JSON，不要返回 Markdown 或额外解释。

            用户基础资料：
            - 昵称：${profile.nickname}
            - 年龄：${profile.age ?: "未填写"}
            - 性别：${profile.gender.ifBlank { "未填写" }}
            - 学历：${profile.education.ifBlank { "未填写" }}
            - 学校：${profile.school.ifBlank { "未填写" }}
            - 专业：${profile.major.ifBlank { "未填写" }}
            - 当前年级：${profile.grade.ifBlank { "未填写" }}
            - 期望行业：${profile.expectedIndustry.ifBlank { "未填写" }}
            - 个人优势：${profile.strengths.joinToString("、").ifBlank { "未填写" }}
            - 兴趣爱好：${profile.hobbies.joinToString("、").ifBlank { "未填写" }}

            MBTI 结果：
            - 类型：${request.mbtiResult.type}
            - 四组维度得分：${request.mbtiResult.dimensionScores}

            霍兰德结果：
            - RIASEC 排名前三项：${request.hollandResult.topCode}
            - 各维度分数：${request.hollandResult.dimensionScores}

            用户目标和偏好：
            ${request.supplement.ifBlank { "用户暂未填写补充说明，可按普通大学生职业规划场景给出稳妥建议。" }}

            请严格按下面 JSON 格式返回：
            {
              "summary": "总体职业倾向分析",
              "personalityStrengths": ["优势1", "优势2", "优势3"],
              "suitableIndustries": ["行业1", "行业2", "行业3"],
              "suitablePositions": ["岗位1", "岗位2", "岗位3"],
              "learningSuggestions": ["建议1", "建议2", "建议3"],
              "actionPlan": {
                "shortTerm": "未来3个月建议",
                "midTerm": "未来6-12个月建议",
                "longTerm": "未来1-3年建议"
              },
              "risks": ["风险1", "风险2"],
              "finalAdvice": "总结建议"
            }
        """.trimIndent()
    }
}
