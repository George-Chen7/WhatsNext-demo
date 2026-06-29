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
        val grade = profile.grade.ifBlank { "当前阶段" }
        val targetCareer = profile.targetCareer.ifBlank { "职业目标方向" }
        val industry = profile.expectedIndustry.ifBlank { "软件开发、数字化服务、数据分析" }
        val strengths = profile.strengths.joinToString("、").ifBlank { "学习能力、项目实践和自我复盘" }

        return AiAnalysisResponse(
            summary = "${profile.nickname} 当前为 ${grade}，目标职业方向是 ${targetCareer}。MBTI 倾向为 ${request.mbtiResult.type}，霍兰德前三项为 ${request.hollandResult.topCode}。综合 ${major} 背景、${industry} 行业兴趣、${strengths} 等个人优势和当前测评结果，建议优先围绕目标岗位建立项目作品与学习计划。该结论用于职业探索参考，不作为单一决定依据。",
            personalityStrengths = listOf(
                "能从兴趣和任务反馈中持续寻找适合自己的方向，适合通过项目作品验证职业选择。",
                "具备将测评结果转化为学习计划的基础，适合循序渐进积累作品集。",
                "如果能补强沟通表达和行业认知，会更容易在实习和校招中说明个人优势。"
            ),
            suitableIndustries = listOf(
                industry,
                "数字化服务与软件行业",
                "教育科技、数据分析相关行业"
            ),
            suitablePositions = listOf(
                targetCareer,
                "数据分析实习生",
                "Android 应用开发 / 前端开发实习生"
            ),
            learningSuggestions = listOf(
                "围绕一个职业规划 App 或校园服务 App 完成可展示项目，沉淀需求文档、原型和代码说明。",
                "补充 SQL、数据可视化、基础 Python 或 Kotlin 项目能力，让测评结果能对应具体技能证据。",
                "每周复盘一次岗位 JD，把高频技能整理成学习清单，并用小项目验证。"
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
