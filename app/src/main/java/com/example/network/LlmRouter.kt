package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.ChatMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object LlmRouter {
    private const val TAG = "LlmRouter"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Streams chat responses from Gemini or Custom Providers.
     */
    suspend fun streamChat(
        providerName: String,
        baseUrl: String,
        apiKey: String,
        modelName: String,
        messages: List<ChatMessage>,
        systemInstruction: String,
        onChunkReceived: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val isGemini = providerName.equals("Gemini", ignoreCase = true) || 
                           providerName.equals("Default Gemini", ignoreCase = true)
            val isClaude = providerName.equals("Claude", ignoreCase = true) || 
                           providerName.equals("Anthropic", ignoreCase = true)

            val request = when {
                isGemini -> buildGeminiRequest(modelName, apiKey, messages, systemInstruction)
                isClaude -> buildAnthropicRequest(apiKey, modelName, messages, systemInstruction)
                else -> buildOpenAiRequest(providerName, baseUrl, apiKey, modelName, messages, systemInstruction)
            }

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    throw Exception("API call failed with code ${response.code}: $errorBody")
                }

                val body = response.body ?: throw Exception("Response body is null")
                val stream = body.byteStream()
                val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                
                var line: String?
                val fullResponseBuilder = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isEmpty()) continue

                    try {
                        val chunkText = when {
                            isGemini -> parseGeminiChunk(currentLine)
                            isClaude -> parseAnthropicChunk(currentLine)
                            else -> parseOpenAiChunk(currentLine)
                        }

                        if (chunkText != null) {
                            fullResponseBuilder.append(chunkText)
                            // Deliver chunk to main thread / Flow
                            withContext(Dispatchers.Main) {
                                onChunkReceived(chunkText)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing line: $currentLine", e)
                    }
                }

                val finalOutput = fullResponseBuilder.toString()
                withContext(Dispatchers.Main) {
                    onComplete(finalOutput)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error streaming response", t)
            withContext(Dispatchers.Main) {
                onError(t)
            }
        }
    }

    private fun buildGeminiRequest(
        model: String,
        apiKey: String,
        messages: List<ChatMessage>,
        systemInstruction: String
    ): Request {
        // Correct model endpoint: streamGenerateContent
        val formattedModel = if (model.contains("/")) model else "models/$model"
        val resolvedKey = apiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        val url = "https://generativelanguage.googleapis.com/v1beta/$formattedModel:streamGenerateContent?key=$resolvedKey"

        // Map messages into Gemini syntax: role is user or model
        val contentsArray = JSONArray()
        for (msg in messages) {
            val contentObj = JSONObject()
            val role = if (msg.role.equals("assistant", ignoreCase = true)) "model" else "user"
            contentObj.put("role", role)

            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", msg.content)
            partsArray.put(partObj)
            
            if (msg.imageBase64 != null) {
                val inlineDataObj = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", msg.imageBase64)
                inlineDataObj.put("inlineData", inlineData)
                partsArray.put(inlineDataObj)
            }
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        val root = JSONObject()
        root.put("contents", contentsArray)

        if (systemInstruction.isNotEmpty()) {
            val systemObj = JSONObject()
            val systemParts = JSONArray()
            val part = JSONObject()
            part.put("text", systemInstruction)
            systemParts.put(part)
            systemObj.put("parts", systemParts)
            root.put("systemInstruction", systemObj)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = root.toString().toRequestBody(mediaType)

        return Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
    }

    private fun buildOpenAiRequest(
        providerName: String,
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemInstruction: String
    ): Request {
        val sanitizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val url = if (sanitizedBaseUrl.contains("chat/completions")) {
            sanitizedBaseUrl
        } else {
            "${sanitizedBaseUrl}chat/completions"
        }

        val messagesArray = JSONArray()
        
        // Add system message if present
        if (systemInstruction.isNotEmpty()) {
            val sysMsg = JSONObject()
            sysMsg.put("role", "system")
            sysMsg.put("content", systemInstruction)
            messagesArray.put(sysMsg)
        }

        // Add historical chat messages
        for (msg in messages) {
            val msgObj = JSONObject()
            msgObj.put("role", msg.role)
            if (msg.imageBase64 != null && msg.role == "user") {
                val contentArray = JSONArray()
                val textObj = JSONObject()
                textObj.put("type", "text")
                textObj.put("text", msg.content)
                contentArray.put(textObj)

                val imageObj = JSONObject()
                imageObj.put("type", "image_url")
                val imageUrlObj = JSONObject()
                imageUrlObj.put("url", "data:image/jpeg;base64,${msg.imageBase64}")
                imageObj.put("image_url", imageUrlObj)
                contentArray.put(imageObj)

                msgObj.put("content", contentArray)
            } else {
                msgObj.put("content", msg.content)
            }
            messagesArray.put(msgObj)
        }

        val root = JSONObject()
        root.put("model", model)
        root.put("messages", messagesArray)
        root.put("stream", true)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = root.toString().toRequestBody(mediaType)

        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
    }

    private fun parseGeminiChunk(line: String): String? {
        // Clean line from brackets and commas if it's sent in dynamic arrays
        var cleaned = line.trim()
        if (cleaned.startsWith("[")) cleaned = cleaned.substring(1)
        if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length - 1)
        if (cleaned.startsWith(",")) cleaned = cleaned.substring(1)
        cleaned = cleaned.trim()

        if (cleaned.isEmpty()) return null

        return try {
            val json = JSONObject(cleaned)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            firstPart?.optString("text")
        } catch (e: Exception) {
            // Self-healing regex fallback for resilient rendering
            val textRegex = "\"text\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            val match = textRegex.find(line)
            match?.groupValues?.get(1)?.replace("\\n", "\n")?.replace("\\t", "\t")
        }
    }

    private fun parseOpenAiChunk(line: String): String? {
        if (!line.startsWith("data:")) return null
        val dataContent = line.substring(5).trim()
        if (dataContent.equals("[DONE]", ignoreCase = true)) return null

        return try {
            val json = JSONObject(dataContent)
            val choices = json.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val delta = firstChoice?.optJSONObject("delta")
            delta?.optString("content")
        } catch (e: Exception) {
            null
        }
    }

    private fun buildAnthropicRequest(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        systemInstruction: String
    ): Request {
        val url = "https://api.anthropic.com/v1/messages"
        val root = JSONObject()
        root.put("model", model.ifEmpty { "claude-3-5-sonnet-latest" })
        root.put("max_tokens", 4000)
        root.put("stream", true)

        if (systemInstruction.isNotEmpty()) {
            root.put("system", systemInstruction)
        }

        val messagesArray = JSONArray()
        for (msg in messages) {
            val msgObj = JSONObject()
            msgObj.put("role", if (msg.role.equals("assistant", ignoreCase = true)) "assistant" else "user")
            
            if (msg.imageBase64 != null && msg.role == "user") {
                val contentArray = JSONArray()

                val imageObj = JSONObject()
                imageObj.put("type", "image")
                val sourceObj = JSONObject()
                sourceObj.put("type", "base64")
                sourceObj.put("media_type", "image/jpeg")
                sourceObj.put("data", msg.imageBase64)
                imageObj.put("source", sourceObj)
                contentArray.put(imageObj)

                val textObj = JSONObject()
                textObj.put("type", "text")
                textObj.put("text", msg.content)
                contentArray.put(textObj)

                msgObj.put("content", contentArray)
            } else {
                msgObj.put("content", msg.content)
            }
            messagesArray.put(msgObj)
        }
        root.put("messages", messagesArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = root.toString().toRequestBody(mediaType)

        return Request.Builder()
            .url(url)
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .post(requestBody)
            .build()
    }

    private fun parseAnthropicChunk(line: String): String? {
        if (!line.startsWith("data:")) return null
        val dataContent = line.substring(5).trim()
        
        return try {
            val json = JSONObject(dataContent)
            val type = json.optString("type")
            if (type == "content_block_delta") {
                val delta = json.optJSONObject("delta")
                delta?.optString("text")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun testConnection(
        providerName: String,
        baseUrl: String,
        apiKey: String,
        modelName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = when {
                providerName.equals("Gemini", ignoreCase = true) || providerName.equals("Default Gemini", ignoreCase = true) -> {
                    val formattedModel = if (modelName.contains("/")) modelName else "models/$modelName"
                    val url = "https://generativelanguage.googleapis.com/v1beta/$formattedModel?key=$apiKey"
                    val request = Request.Builder().url(url).get().build()
                    okHttpClient.newCall(request).execute()
                }
                providerName.equals("Claude", ignoreCase = true) || providerName.equals("Anthropic", ignoreCase = true) -> {
                    val url = "https://api.anthropic.com/v1/messages"
                    val root = JSONObject()
                    root.put("model", modelName.ifEmpty { "claude-3-5-sonnet-latest" })
                    root.put("max_tokens", 10)
                    val messagesArray = JSONArray()
                    val userMsg = JSONObject()
                    userMsg.put("role", "user")
                    userMsg.put("content", "ping")
                    messagesArray.put(userMsg)
                    root.put("messages", messagesArray)

                    val requestBody = root.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01")
                        .header("content-type", "application/json")
                        .post(requestBody)
                        .build()
                    okHttpClient.newCall(request).execute()
                }
                providerName.equals("Cohere", ignoreCase = true) -> {
                    val url = "https://api.cohere.com/v1/chat"
                    val root = JSONObject()
                    root.put("message", "ping")
                    root.put("model", modelName)
                    val requestBody = root.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $apiKey")
                        .post(requestBody)
                        .build()
                    okHttpClient.newCall(request).execute()
                }
                providerName.equals("Tavily", ignoreCase = true) -> {
                    val url = "https://api.tavily.com/search"
                    val root = JSONObject()
                    root.put("api_key", apiKey)
                    root.put("query", "test claw handshake")
                    val requestBody = root.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
                    okHttpClient.newCall(request).execute()
                }
                else -> {
                    val sanitizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                    val chatUrl = if (sanitizedBaseUrl.contains("chat/completions")) {
                        sanitizedBaseUrl
                    } else {
                        "${sanitizedBaseUrl}chat/completions"
                    }
                    val root = JSONObject()
                    root.put("model", modelName)
                    root.put("max_tokens", 5)
                    val messagesArray = JSONArray()
                    val sysMsg = JSONObject()
                    sysMsg.put("role", "user")
                    sysMsg.put("content", "ping")
                    messagesArray.put(sysMsg)
                    root.put("messages", messagesArray)

                    val requestBody = root.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(chatUrl)
                        .header("Authorization", "Bearer $apiKey")
                        .post(requestBody)
                        .build()
                    okHttpClient.newCall(request).execute()
                }
            }

            response.use { res ->
                val bodyText = res.body?.string() ?: ""
                if (res.isSuccessful) {
                    Result.success("Success! Handshake verified on $providerName. Code: ${res.code}")
                } else {
                    var errorDetail = "Failed with Code ${res.code}"
                    try {
                        val errorJson = JSONObject(bodyText)
                        if (errorJson.has("error")) {
                            val errObj = errorJson.optJSONObject("error")
                            errorDetail += ": " + (errObj?.optString("message") ?: errorJson.optString("error"))
                        } else if (errorJson.has("message")) {
                            errorDetail += ": " + errorJson.optString("message")
                        } else {
                            errorDetail += ": ${bodyText.take(160)}"
                        }
                    } catch (e: Exception) {
                        if (bodyText.isNotEmpty()) {
                            errorDetail += ": ${bodyText.take(160)}"
                        }
                    }
                    Result.failure(Exception(errorDetail))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
