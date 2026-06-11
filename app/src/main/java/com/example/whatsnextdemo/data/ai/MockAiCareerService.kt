package com.example.whatsnextdemo.data.ai

import com.example.whatsnextdemo.data.model.ActionPlan
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse
import kotlinx.coroutines.delay

class MockAiCareerService : AiCareerService {
    override suspend fun generateAnalysis(request: AiAnalysisRequest): AiAnalysisResponse {
        delay(900)
        val profile = request.userProfile
        val major = profile.major.ifBlank { "当前专业" }
        val industry = profile.expectedIndustry.ifBlank { "AI 应用、软件开发、数据分析" }

        return AiAnalysisResponse(
            summary = "${profile.nickname} 的 MBTI 倾向为 ${request.mbtiResult.type}，霍兰德前三项为 ${request.hollandResult.topCode}，职业能力优势集中在 ${request.careerAbilityResult.topDimensions}，职业锚偏向 ${request.careerAnchorResult.topDimensions}，职业价值观更重视 ${request.careerValuesResult.topDimensions}。综合 ${major} 背景、能力基础和当前测评结果，更适合从技术理解、数据分析、产品表达或业务运营结合的方向切入。该结论用于职业探索参考，不作为单一决定依据。",
            personalityStrengths = listOf(
                "能从兴趣和任务反馈中持续寻找适合自己的方向，适合通过项目作品验证职业选择。",
                "职业能力测评显示 ${request.careerAbilityResult.topDimensions} 较突出，适合把优势转化为课程项目、竞赛作品或实习经历。",
                "职业锚和价值观结果显示 ${request.careerAnchorResult.topDimensions}、${request.careerValuesResult.topDimensions} 是重要偏好，后续选岗位时应同时看工作内容、组织环境和成长节奏。"
            ),
            suitableIndustries = listOf(
                industry,
                "数字化服务与软件行业",
                "教育科技、数据分析相关行业"
            ),
            suitablePositions = listOf(
                "AI 产品助理 / 产品经理实习生",
                "数据分析实习生",
                "Android 应用开发 / 前端开发实习生"
            ),
            learningSuggestions = listOf(
                "围绕一个职业规划 App 或校园服务 App 完成可展示项目，沉淀需求文档、原型和代码说明。",
                "结合职业能力测评分数 ${request.careerAbilityResult.dimensionScores}，优先补齐低分能力，并用小任务验证提升效果。",
                "每周复盘一次岗位 JD，把岗位要求与职业锚 ${request.careerAnchorResult.dimensionScores}、价值观 ${request.careerValuesResult.dimensionScores} 对照，筛选更匹配的目标。"
            ),
            actionPlan = ActionPlan(
                shortTerm = "整理个人资料、测评结果和目标岗位，完成一版简历与 1 个课程设计项目说明。",
                midTerm = "选择 2-3 类岗位持续投递实习，补齐作品集、数据分析案例和面试表达。",
                longTerm = "根据实习反馈确定主方向，在技术、产品或数据方向中选择一个核心能力深耕。"
            ),
            risks = listOf(
                "不要只根据 MBTI 或单次测评决定职业方向，需要结合项目表现、实习反馈和真实岗位要求。",
                "如果目标过宽，学习计划会分散，应优先选择一个主岗位和一个备选岗位。"
            ),
            finalAdvice = "建议先用 3 个月做职业探索和作品集建设，再用 6-12 个月通过实习与项目验证方向。保持可调整的规划，比一次性选择所谓最适合的职业更稳妥。"
        )
    }
}
