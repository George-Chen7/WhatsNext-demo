package com.example.whatsnextdemo.data.ai

import com.example.whatsnextdemo.BuildConfig
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse

class RemoteAiCareerService(
    private val apiKeyProvider: () -> String,
    private val endpointProvider: () -> String
) : AiCareerService {
    constructor() : this(
        apiKeyProvider = { BuildConfig.AI_API_KEY },
        endpointProvider = { BuildConfig.AI_API_ENDPOINT }
    )

    override suspend fun generateAnalysis(request: AiAnalysisRequest): AiAnalysisResponse {
        val apiKey = apiKeyProvider()
        val endpoint = endpointProvider()
        val prompt = AiPromptBuilder.buildCareerAnalysisPrompt(request)

        // TODO Phase 2:
        // 1. For local course demos, put AI_API_KEY and AI_API_ENDPOINT in root local.properties.
        //    local.properties is ignored by Git, and Gradle injects the values into BuildConfig.
        // 2. For production apps, prefer a backend proxy/token API. Do not ship long-lived model keys in APKs.
        // 3. Use Retrofit/OkHttp to POST prompt to OpenAI, DeepSeek, Qwen, or another model provider.
        // 4. Parse the provider JSON into AiAnalysisResponse.fromJsonString(modelContent).
        // 5. Never commit a real API key to Git.
        // Example request body fields usually include model, messages, temperature, and response_format.
        if (apiKey.isBlank() || endpoint.isBlank() || prompt.isBlank()) {
            error("Remote AI service is not configured. Use MockAiCareerService for the course demo.")
        }
        error("Remote AI API integration is reserved for Phase 2.")
    }
}
