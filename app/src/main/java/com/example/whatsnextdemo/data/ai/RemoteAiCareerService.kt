package com.example.whatsnextdemo.data.ai

import android.util.Log
import com.example.whatsnextdemo.BuildConfig
import com.example.whatsnextdemo.data.model.AiAnalysisRequest
import com.example.whatsnextdemo.data.model.AiAnalysisResponse
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class RemoteAiCareerService(
    private val apiKeyProvider: () -> String,
    private val endpointProvider: () -> String,
    private val modelProvider: () -> String
) : AiCareerService {
    constructor() : this(
        apiKeyProvider = { BuildConfig.AI_API_KEY },
        endpointProvider = { BuildConfig.AI_API_ENDPOINT },
        modelProvider = { BuildConfig.AI_MODEL }
    )

    override suspend fun generateAnalysis(request: AiAnalysisRequest): AiAnalysisResponse {
        val apiKey: String = apiKeyProvider().trim()
        val endpoint: String = endpointProvider().trim()
        val model: String = modelProvider().trim()
        if (apiKey.isBlank()) {
            throw AiConfigurationException("AI_API_KEY 未配置，请在 local.properties 或环境变量中配置后重新编译。")
        }
        if (endpoint.isBlank()) {
            throw AiConfigurationException("AI_API_ENDPOINT 未配置，请在 local.properties 或环境变量中配置后重新编译。")
        }
        if (model.isBlank()) {
            throw AiConfigurationException("AI_MODEL 未配置，请在 local.properties 或环境变量中配置后重新编译。")
        }

        val prompt: String = AiPromptBuilder.buildCareerAnalysisPrompt(request)
        val requestBody: String = buildRequestBody(prompt, model)
        val url: URL = URL(buildChatCompletionsUrl(endpoint))
        val responseBody: String = postWithRetry(url, apiKey, requestBody)
        val modelContent: String = extractModelContent(responseBody)
        return parseModelContent(modelContent)
    }

    private fun buildRequestBody(prompt: String, model: String): String {
        val messages: JSONArray = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put("content", "你只输出严格 JSON，不输出 Markdown、代码块或额外解释。")
            )
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            )
        return JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.4)
            .put("response_format", JSONObject().put("type", "json_object"))
            .toString()
    }

    private fun buildChatCompletionsUrl(endpoint: String): String {
        val trimmedEndpoint: String = endpoint.trimEnd('/')
        return if (trimmedEndpoint.endsWith("/chat/completions")) {
            trimmedEndpoint
        } else if (trimmedEndpoint.endsWith("/v1")) {
            "$trimmedEndpoint/chat/completions"
        } else {
            "$trimmedEndpoint/chat/completions"
        }
    }

    private fun postWithRetry(url: URL, apiKey: String, requestBody: String): String {
        var lastError: Exception? = null
        for (attempt: Int in 1..MAX_ATTEMPTS) {
            try {
                return postOnce(url, apiKey, requestBody)
            } catch (error: AiHttpException) {
                lastError = error
                if (!error.isRetryable || attempt == MAX_ATTEMPTS) {
                    throw error
                }
                logRetry(attempt, url, error)
                Thread.sleep(RETRY_DELAY_MILLIS * attempt)
            } catch (error: java.io.IOException) {
                lastError = error
                if (attempt == MAX_ATTEMPTS) {
                    throw AiNetworkException("AI 请求网络异常，url=${url}, message=${error.message}", error)
                }
                logRetry(attempt, url, error)
                Thread.sleep(RETRY_DELAY_MILLIS * attempt)
            }
        }
        throw AiNetworkException("AI 请求失败，url=${url}, message=${lastError?.message}", lastError)
    }

    private fun postOnce(url: URL, apiKey: String, requestBody: String): String {
        val connection: HttpURLConnection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer: OutputStreamWriter ->
                writer.write(requestBody)
            }
            val statusCode: Int = connection.responseCode
            val responseBody: String = readResponseBody(connection, statusCode)
            if (statusCode !in HTTP_SUCCESS_RANGE) {
                throw AiHttpException(
                    statusCode = statusCode,
                    responseBody = responseBody.take(MAX_ERROR_BODY_LENGTH),
                    requestUrl = url.toString()
                )
            }
            responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponseBody(connection: HttpURLConnection, statusCode: Int): String {
        val stream: InputStream? = if (statusCode in HTTP_SUCCESS_RANGE) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        if (stream == null) {
            return ""
        }
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader: BufferedReader ->
            reader.readText()
        }
    }

    private fun extractModelContent(responseBody: String): String {
        try {
            val root: JSONObject = JSONObject(responseBody)
            val choices: JSONArray = root.getJSONArray("choices")
            if (choices.length() == 0) {
                throw AiResponseParseException("AI 响应缺少 choices，responseBody=${responseBody.take(MAX_ERROR_BODY_LENGTH)}")
            }
            val firstChoice: JSONObject = choices.getJSONObject(0)
            val message: JSONObject = firstChoice.getJSONObject("message")
            val content: String = message.getString("content")
            if (content.isBlank()) {
                throw AiResponseParseException("AI 响应 content 为空，responseBody=${responseBody.take(MAX_ERROR_BODY_LENGTH)}")
            }
            return content
        } catch (error: JSONException) {
            throw AiResponseParseException(
                "AI 接口响应格式异常，responseBody=${responseBody.take(MAX_ERROR_BODY_LENGTH)}",
                error
            )
        }
    }

    private fun parseModelContent(modelContent: String): AiAnalysisResponse {
        try {
            return AiAnalysisResponse.fromJsonString(modelContent)
        } catch (error: JSONException) {
            throw AiResponseParseException(
                "AI 返回内容不是约定 JSON 结构，content=${modelContent.take(MAX_ERROR_BODY_LENGTH)}",
                error
            )
        }
    }

    private fun logRetry(attempt: Int, url: URL, error: Exception): Unit {
        val fields: JSONObject = JSONObject()
            .put("event", "remote_ai_retry")
            .put("attempt", attempt)
            .put("url", url.toString())
            .put("errorType", error.javaClass.simpleName)
            .put("message", error.message)
        Log.w(TAG, fields.toString(), error)
    }

    companion object {
        private const val TAG: String = "RemoteAiCareerService"
        private const val MAX_ATTEMPTS: Int = 3
        private const val RETRY_DELAY_MILLIS: Long = 800L
        private const val CONNECT_TIMEOUT_MILLIS: Int = 15_000
        private const val READ_TIMEOUT_MILLIS: Int = 60_000
        private const val MAX_ERROR_BODY_LENGTH: Int = 1_500
        private val HTTP_SUCCESS_RANGE: IntRange = 200..299
    }
}

class AiConfigurationException(message: String) : IllegalStateException(message)

class AiNetworkException(message: String, cause: Throwable?) : java.io.IOException(message, cause)

class AiResponseParseException(message: String, cause: Throwable?) : IllegalStateException(message, cause) {
    constructor(message: String) : this(message, null)
}

class AiHttpException(
    val statusCode: Int,
    val responseBody: String,
    val requestUrl: String
) : java.io.IOException(
    "AI 接口请求失败，statusCode=$statusCode, requestUrl=$requestUrl, responseBody=$responseBody"
) {
    val isRetryable: Boolean = statusCode == 408 || statusCode == 429 || statusCode in 500..599
}
