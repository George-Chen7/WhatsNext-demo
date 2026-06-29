package com.example.whatsnextdemo.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whatsnextdemo.data.ai.AiCareerRepository
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.database.entity.UserEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse
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

    fun generateAnalysis(): Unit {
        if (_uiState.value is AiAnalysisUiState.Loading) {
            return
        }
        _uiState.value = AiAnalysisUiState.Loading
        viewModelScope.launch {
            val requestResult: Result<AiAnalysisRequest> = withContext(Dispatchers.IO) { buildRequest() }
            val request: AiAnalysisRequest = requestResult.getOrElse {
                _uiState.value = AiAnalysisUiState.Error(it.message ?: "生成 AI 分析请求失败")
                return@launch
            }

            val responseResult: Result<AiAnalysisResponse> = withContext(Dispatchers.IO) {
                aiCareerRepository.generateAnalysis(request)
                    .onSuccess { response: AiAnalysisResponse ->
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
            val username: String = sessionManager.getUsername()
            if (username.isBlank()) {
                throw IllegalStateException("请先登录后再生成 AI 职业分析报告")
            }

            val user: UserEntity = userRepository.findUser(username)
                ?: throw IllegalStateException("未找到当前用户资料，请重新登录后补充资料")
            validateUserProfile(user)

            val results: List<AssessmentResultEntity> = assessmentRepository.getResults(username)
            val mbti: AssessmentResultEntity = results.firstOrNull { it.type == AssessmentScorer.TYPE_MBTI }
                ?: error("请先完成 MBTI 测评")
            val holland: AssessmentResultEntity = results.firstOrNull { it.type == AssessmentScorer.TYPE_HOLLAND }
                ?: error("请先完成霍兰德测评")

            val supplement: String = buildProfileSupplement(user)
            val profile: UserProfile = UserProfile(
                username = username,
                nickname = user.nickname.orEmpty(),
                education = "",
                major = user.major.orEmpty(),
                grade = user.grade.orEmpty(),
                targetCareer = user.targetCareer.orEmpty(),
                expectedIndustry = user.interestedIndustry.orEmpty(),
                strengths = parseTextItems(user.strengths.orEmpty()),
                hobbies = emptyList(),
                extraNotes = supplement,
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
        val json: JSONObject = JSONObject(scoreDetail)
        return json.keys().asSequence()
            .associateWith { key: String -> json.getInt(key) }
    }

    private fun parseTextItems(value: String): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }
        return value
            .split("、", "，", ",", "\n")
            .map { item: String -> item.trim() }
            .filter { item: String -> item.isNotBlank() }
    }

    private fun validateUserProfile(user: UserEntity): Unit {
        val missingFields: MutableList<String> = mutableListOf()
        if (user.nickname.isNullOrBlank()) missingFields.add("昵称")
        if (user.major.isNullOrBlank()) missingFields.add("专业")
        if (user.grade.isNullOrBlank()) missingFields.add("年级")
        if (user.targetCareer.isNullOrBlank()) missingFields.add("目标职业方向")
        if (missingFields.isNotEmpty()) {
            throw IllegalStateException("请先补充个人资料：${missingFields.joinToString("、")}")
        }
    }

    private fun buildProfileSupplement(user: UserEntity): String {
        return listOf(
            "年级：${user.grade.orEmpty()}",
            "目标职业方向：${user.targetCareer.orEmpty()}",
            "感兴趣的行业：${user.interestedIndustry.orEmpty().ifBlank { "未填写" }}",
            "个人优势：${user.strengths.orEmpty().ifBlank { "未填写" }}"
        ).joinToString(separator = "\n")
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
