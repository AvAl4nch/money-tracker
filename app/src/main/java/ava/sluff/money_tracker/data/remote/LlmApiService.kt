package ava.sluff.money_tracker.data.remote

import android.util.Log
import ava.sluff.money_tracker.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmApiService @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {

    /** Single-turn call used by SMS categorization. */
    suspend fun chatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userMessage: String
    ): String? = chatCompletion(
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        messages = listOf(Message("system", systemPrompt), Message("user", userMessage)),
        temperature = 0.1
    )

    /** Multi-turn call used by the Ask tab, where prior turns and temperature both matter. */
    suspend fun chatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<Message>,
        temperature: Double
    ): String? = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            Log.e(TAG, "Base URL is empty, skipping API call")
            return@withContext null
        }
        val url = baseUrl.trimEnd('/') + "/v1/chat/completions"
        Log.d(TAG, "API call to $url model=$model")
        val json = gson.toJson(
            ChatRequest(model = model, messages = messages, temperature = temperature)
        )
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        try {
            val response = client.newCall(request).execute()
            Log.d(TAG, "API response code=${response.code}")
            if (!response.isSuccessful) {
                val errorBody = if (BuildConfig.DEBUG) response.body?.string() else "(body logging disabled in release)"
                Log.e(TAG, "API error: ${response.code} $errorBody")
                return@withContext null
            }
            val responseBody = response.body?.string() ?: return@withContext null
            if (BuildConfig.DEBUG) Log.d(TAG, "API response body=${responseBody.take(200)}")
            gson.fromJson(responseBody, ChatResponse::class.java)
                .choices?.firstOrNull()?.message?.content
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            null
        }
    }

    companion object {
        private const val TAG = "MoneyTracker.API"
    }
}
