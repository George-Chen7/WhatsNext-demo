package com.example.whatsnextdemo.data.ai

import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse

interface AiCareerService {
    suspend fun generateAnalysis(request: AiAnalysisRequest): AiAnalysisResponse
}
