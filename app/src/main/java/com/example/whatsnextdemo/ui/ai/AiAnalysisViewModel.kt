package com.example.whatsnextdemo.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whatsnextdemo.data.ai.AiCareerRepository
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.HollandResult
import com.example.whatsnextdemo.data.model.MbtiResult
import com.example.whatsnextdemo.data.model.UserProfile
import com.example.whatsnextdemo.data.repository.AssessmentRepository
import com.example.whatsnextdemo.data.repository.CareerReportRepository
import com.example.whatsnextdemo.data.repository.UserRepository
import com.example.whatsnextdemo.ui.assessment.AssessmentScorer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AiAnalysisViewModel(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val assessmentRepository: AssessmentRepository,
    private val careerReportRepository: CareerReportRepository,
    private val aiCareerRepository: AiCareerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val uiState: StateFlow<AiAnalysisUiState> = _uiState.asStateFlow()

    fun generateAnalysis() {
        _uiState.value = AiAnalysisUiState.Loading
        viewModelScope.launch {
            val requestResult = withContext(Dispatchers.IO) { buildRequest() }
            val request = requestResult.getOrElse {
                _uiState.value = AiAnalysisUiState.Error(it.message ?: "生成 AI 分析请求失败")
                return@launch
            }

            val responseResult = withContext(Dispatchers.IO) {
                aiCareerRepository.generateAnalysis(request).onSuccess { response ->
                    careerReportRepository.saveReport(
                        CareerReportEntity(
                            username = request.userProfile.username,
                            title = "AI 职业分析报告",
                            content = response.toDisplayText(),
                            mbti = request.mbtiResult.type,
                            holland = request.hollandResult.topCode
                        )
                    )
                }
            }

            responseResult
                .onSuccess {
                    sessionManager.setFirstReportGenerated()
                    _uiState.value = AiAnalysisUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = AiAnalysisUiState.Error(it.message ?: "AI 分析失败，请稍后重试")
                }
        }
    }

    private suspend fun buildRequest(): Result<AiAnalysisRequest> {
        return runCatching {
            val username = sessionManager.getUsername().ifBlank { "guest" }
            val user = userRepository.findUser(username)
            val results = assessmentRepository.getResults(username)
            val mbti = results.firstOrNull { it.type == AssessmentScorer.TYPE_MBTI }
                ?: error("请先完成 MBTI 测评")
            val holland = results.firstOrNull { it.type == AssessmentScorer.TYPE_HOLLAND }
                ?: error("请先完成霍兰德测评")

            val profile = UserProfile(
                username = username,
                nickname = user?.nickname.orEmpty().ifBlank { username },
                education = "本科",
                major = user?.major.orEmpty(),
                expectedIndustry = "AI 应用、软件开发、数据分析",
                strengths = listOf("学习能力", "项目实践", "自我复盘"),
                hobbies = listOf("技术学习", "职业探索"),
                extraNotes = "当前阶段优先保证课程设计演示稳定，真实求职偏好可在资料页扩展填写。",
                mbti = mbti.result,
                holland = holland.result
            )

            AiAnalysisRequest(
                userProfile = profile,
                mbtiResult = MbtiResult(
                    type = mbti.result,
                    dimensionScores = parseScores(mbti.scoreDetail)
                ),
                hollandResult = HollandResult(
                    topCode = holland.result,
                    dimensionScores = parseScores(holland.scoreDetail)
                ),
                supplement = profile.extraNotes
            )
        }
    }

    private fun parseScores(scoreDetail: String): Map<String, Int> {
        val json = JSONObject(scoreDetail)
        return json.keys().asSequence()
            .associateWith { key -> json.optInt(key) }
    }

    class Factory(
        private val sessionManager: SessionManager,
        private val userRepository: UserRepository,
        private val assessmentRepository: AssessmentRepository,
        private val careerReportRepository: CareerReportRepository,
        private val aiCareerRepository: AiCareerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AiAnalysisViewModel(
                sessionManager,
                userRepository,
                assessmentRepository,
                careerReportRepository,
                aiCareerRepository
            ) as T
        }
    }
}
