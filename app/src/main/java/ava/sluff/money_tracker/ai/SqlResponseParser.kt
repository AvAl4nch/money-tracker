package ava.sluff.money_tracker.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/** What the model decided to do with a question: run [sql], or reply with [answer] directly. */
data class SqlPlan(val sql: String?, val answer: String?)

@Singleton
class SqlResponseParser @Inject constructor(
    private val gson: Gson
) {

    fun parse(response: String): SqlPlan? {
        val stripped = response.replace("```json", "").replace("```", "")
        val start = stripped.indexOf('{')
        val end = stripped.lastIndexOf('}')
        if (start !in 0 until end) return null
        val obj = try {
            gson.fromJson(stripped.substring(start, end + 1), JsonObject::class.java)
        } catch (e: Exception) {
            null
        } ?: return null

        val sql = obj.get("sql")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
        val answer = obj.get("answer")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
        return if (sql == null && answer == null) null else SqlPlan(sql, answer)
    }
}
