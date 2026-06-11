package ava.sluff.money_tracker.ai

import ava.sluff.money_tracker.domain.model.CategorizationResult
import ava.sluff.money_tracker.domain.model.TransactionType
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseParser @Inject constructor(
    private val gson: Gson
) {

    fun parse(response: String): CategorizationResult? {
        val stripped = response
            .replace("```json", "")
            .replace("```", "")
        val start = stripped.indexOf('{')
        val end = stripped.lastIndexOf('}')
        val cleaned = if (start in 0 until end) stripped.substring(start, end + 1) else stripped.trim()
        return try {
            val obj = gson.fromJson(cleaned, JsonObject::class.java) ?: return null
            val amount = obj.get("amount")?.takeIf { !it.isJsonNull }?.asDouble ?: return null
            val type = when (obj.get("type")?.takeIf { !it.isJsonNull }?.asString?.uppercase()) {
                "CREDIT" -> TransactionType.CREDIT
                else -> TransactionType.DEBIT
            }
            val confidence = (obj.get("confidence")?.takeIf { !it.isJsonNull }?.asFloat ?: 0f)
                .coerceIn(0f, 1f)
            CategorizationResult(
                categoryName = obj.get("category")?.takeIf { !it.isJsonNull }?.asString ?: "Other",
                merchantName = obj.get("merchant")?.takeIf { !it.isJsonNull }?.asString,
                description = obj.get("description")?.takeIf { !it.isJsonNull }?.asString,
                confidence = confidence,
                type = type,
                amount = amount,
                balanceAfter = obj.get("balance_after")?.takeIf { !it.isJsonNull }?.asDouble
            )
        } catch (e: Exception) {
            null
        }
    }
}
