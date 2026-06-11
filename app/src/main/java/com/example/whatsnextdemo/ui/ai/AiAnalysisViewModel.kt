package com.example.whatsnextdemo.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whatsnextdemo.data.ai.AiCareerRepository
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.local.SessionManager
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.CareerDimensionResult
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
import java.util.Calendar

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
            val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
            val mbti = results.firstOrNull { it.type == AssessmentScorer.TYPE_MBTI }
                ?: error("请先完成 MBTI 测评")
            val holland = results.firstOrNull { it.type == AssessmentScorer.TYPE_HOLLAND }
                ?: error("请先完成霍兰德测评")
            val careerAbility = results.firstOrNull { it.type == AssessmentScorer.TYPE_CAREER_ABILITY }
                ?: error("请先完成职业能力测评")
            val careerAnchor = results.firstOrNull { it.type == AssessmentScorer.TYPE_CAREER_ANCHOR }
                ?: error("请先完成职业锚测评")
            val careerValues = results.firstOrNull { it.type == AssessmentScorer.TYPE_CAREER_VALUES }
                ?: error("请先完成职业价值观测评")

            val profile = UserProfile(
                username = username,
                nickname = user?.nickname.orEmpty().ifBlank { username },
                age = user?.birthYear?.let { birthYear -> currentYear - birthYear },
                gender = user?.gender.orEmpty(),
                education = user?.education.orEmpty(),
                major = user?.major.orEmpty(),
                school = user?.schoolType.orEmpty(),
                grade = user?.grade.orEmpty(),
                expectedIndustry = user?.expectedIndustries.orEmpty(),
                strengths = splitProfileValues(user?.targetPositions),
                hobbies = splitProfileValues(user?.expectedIndustries),
                extraNotes = buildProfileNotes(
                    graduationPlan = user?.graduationPlan.orEmpty(),
                    englishLevels = user?.englishLevels.orEmpty()
                ),
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
                careerAbilityResult = CareerDimensionResult(
                    topDimensions = careerAbility.result,
                    dimensionScores = parseScores(careerAbility.scoreDetail)
                ),
                careerAnchorResult = CareerDimensionResult(
                    topDimensions = careerAnchor.result,
                    dimensionScores = parseScores(careerAnchor.scoreDetail)
                ),
                careerValuesResult = CareerDimensionResult(
                    topDimensions = careerValues.result,
                    dimensionScores = parseScores(careerValues.scoreDetail)
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

    private fun splitProfileValues(values: String?): List<String> {
        if (values.isNullOrBlank()) {
            return emptyList()
        }
        return values.split(PROFILE_LIST_SEPARATOR)
            .map { value -> value.trim() }
            .filter { value -> value.isNotEmpty() }
    }

    private fun buildProfileNotes(graduationPlan: String, englishLevels: String): String {
        return "毕业计划：${graduationPlan.ifBlank { "未填写" }}；英语水平：${englishLevels.ifBlank { "未填写" }}"
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

    private companion object {
        private const val PROFILE_LIST_SEPARATOR: String = "、"
    }
}
