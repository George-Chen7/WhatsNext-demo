package com.example.whatsnextdemo.data.ai

import android.util.Log
import com.example.whatsnextdemo.BuildConfig
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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

        if (apiKey.isBlank()) {
            error("Remote AI service is not configured: AI_API_KEY is blank.")
        }
        if (endpoint.isBlank()) {
            error("Remote AI service is not configured: AI_API_ENDPOINT is blank.")
        }
        if (prompt.isBlank()) {
            error("Remote AI prompt is blank.")
        }

        val requestUrl = buildChatCompletionsUrl(endpoint)
        val requestBody = buildRequestBody(prompt)
        val responseBody = postWithRetries(
            requestUrl = requestUrl,
            apiKey = apiKey,
            requestBody = requestBody
        )
        val modelContent = parseModelContent(responseBody)
        return parseAnalysisResponse(modelContent)
    }

    private suspend fun postWithRetries(
        requestUrl: String,
        apiKey: String,
        requestBody: String
    ): String {
        var lastError: RemoteAiException? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                return postJson(
                    requestUrl = requestUrl,
                    apiKey = apiKey,
                    requestBody = requestBody
                )
            } catch (error: IOException) {
                lastError = RemoteAiException(
                    "Remote AI request failed: attempt=$attempt, url=$requestUrl, " +
                        "requestBody=$requestBody, cause=${error.message}",
                    error
                )
                logRetryWarning(attempt, requestUrl, lastError)
            } catch (error: RemoteAiHttpException) {
                lastError = error
                logRetryWarning(attempt, requestUrl, error)
            } catch (error: RemoteAiException) {
                lastError = error
                logRetryWarning(attempt, requestUrl, error)
            }
            if (attempt < MAX_ATTEMPTS) {
                delay(RETRY_DELAY_MILLIS * attempt)
            }
        }
        throw lastError ?: RemoteAiException(
            "Remote AI request failed without an available error: url=$requestUrl, requestBody=$requestBody"
        )
    }

    private fun postJson(
        requestUrl: String,
        apiKey: String,
        requestBody: String
    ): String {
        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
        connection.readTimeout = READ_TIMEOUT_MILLIS
        connection.doOutput = true
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { outputStream ->
            outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode
        val responseBody = if (statusCode in HTTP_SUCCESS_RANGE) {
            connection.inputStream.bufferedReader().use { reader -> reader.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { reader -> reader.readText() }.orEmpty()
        }
        connection.disconnect()

        if (statusCode !in HTTP_SUCCESS_RANGE) {
            throw RemoteAiHttpException(
                "Remote AI HTTP error: statusCode=$statusCode, url=$requestUrl, " +
                    "requestBody=$requestBody, responseBody=$responseBody",
                statusCode,
                responseBody
            )
        }
        if (responseBody.isBlank()) {
            throw RemoteAiException(
                "Remote AI response body is blank: statusCode=$statusCode, url=$requestUrl, " +
                    "requestBody=$requestBody"
            )
        }
        return responseBody
    }

    private fun buildChatCompletionsUrl(endpoint: String): String {
        val normalizedEndpoint = endpoint.trim().trimEnd('/')
        return if (normalizedEndpoint.endsWith("/chat/completions")) {
            normalizedEndpoint
        } else {
            "$normalizedEndpoint/chat/completions"
        }
    }

    private fun buildRequestBody(prompt: String): String {
        val messages = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put("content", "你只输出合法 JSON，不输出 Markdown、代码块或解释。")
            )
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            )
        return JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .put("temperature", TEMPERATURE)
            .put(
                "response_format",
                JSONObject().put("type", "json_object")
            )
            .toString()
    }

    private fun parseModelContent(responseBody: String): String {
        try {
            val root = JSONObject(responseBody)
            val choices = root.getJSONArray("choices")
            if (choices.length() == 0) {
                throw RemoteAiException("Remote AI response has no choices: responseBody=$responseBody")
            }
            return choices
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (error: JSONException) {
            throw RemoteAiException(
                "Remote AI response JSON parse failed: responseBody=$responseBody, cause=${error.message}",
                error
            )
        }
    }

    private fun parseAnalysisResponse(modelContent: String): AiAnalysisResponse {
        try {
            return AiAnalysisResponse.fromJsonString(modelContent)
        } catch (error: JSONException) {
            throw RemoteAiException(
                "Remote AI model content JSON parse failed: modelContent=$modelContent, cause=${error.message}",
                error
            )
        }
    }

    private fun logRetryWarning(
        attempt: Int,
        requestUrl: String,
        error: RemoteAiException
    ) {
        val logFields = JSONObject()
            .put("attempt", attempt)
            .put("maxAttempts", MAX_ATTEMPTS)
            .put("requestUrl", requestUrl)
            .put("errorMessage", error.message)
        Log.w(LOG_TAG, logFields.toString(), error)
    }

    companion object {
        private const val LOG_TAG = "RemoteAiCareerService"
        private const val MODEL = "deepseek-chat"
        private const val TEMPERATURE = 0.4
        private const val MAX_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 800L
        private const val CONNECT_TIMEOUT_MILLIS = 15_000
        private const val READ_TIMEOUT_MILLIS = 60_000
        private val HTTP_SUCCESS_RANGE: IntRange = 200..299
    }
}

open class RemoteAiException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class RemoteAiHttpException(
    message: String,
    val statusCode: Int,
    val responseBody: String
) : RemoteAiException(message)
