package com.example.whatsnextdemo.data.ai

import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse

class AiCareerRepository(
    private val service: AiCareerService = MockAiCareerService()
) {
    suspend fun generateAnalysis(request: AiAnalysisRequest): Result<AiAnalysisResponse> {
        return runCatching {
            service.generateAnalysis(request)
        }
    }
}
