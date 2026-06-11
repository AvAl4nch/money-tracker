package ava.sluff.money_tracker.ai

import ava.sluff.money_tracker.data.remote.LlmApiService
import ava.sluff.money_tracker.domain.model.CategorizationResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionCategorizer @Inject constructor(
    private val llmApiService: LlmApiService,
    private val responseParser: ResponseParser
) {

    suspend fun categorize(
        smsBody: String,
        baseUrl: String,
        apiKey: String,
        model: String,
        currency: String
    ): CategorizationResult? {
        val response = llmApiService.chatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = PromptBuilder.systemPrompt(currency),
            userMessage = PromptBuilder.userPrompt(smsBody)
        ) ?: return null
        return responseParser.parse(response)
    }
}
