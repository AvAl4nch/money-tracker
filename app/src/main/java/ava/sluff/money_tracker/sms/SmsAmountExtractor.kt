package ava.sluff.money_tracker.sms

/**
 * Regex fallback used when the LLM is unreachable, so a stored transaction
 * still carries the real amount instead of 0.0 (Phase 1 deferred finding #6).
 */
object SmsAmountExtractor {

    private val amountPatterns = listOf(
        Regex("(?:بقيمة|بمبلغ|مبلغ)\\s*([0-9]+(?:[.,][0-9]+)?)"),
        Regex("(?i)(?:JOD|JD|دينار)\\s*([0-9]+(?:[.,][0-9]+)?)"),
        Regex("(?i)(?:debited|credited|spent|received|withdrawn)\\s+(?:JOD|JD)?\\s*([0-9]+(?:[.,][0-9]+)?)")
    )

    private val balancePatterns = listOf(
        Regex("(?:الرصيد المتوفر|ليصبح رصيدكم|المتبقي)\\s*([0-9]+(?:[.,][0-9]+)?)"),
        Regex("(?i)(?:available\\s+)?balance\\s*:?\\s*([0-9]+(?:[.,][0-9]+)?)")
    )

    fun amount(body: String): Double? = firstMatch(amountPatterns, body)

    fun balanceAfter(body: String): Double? = firstMatch(balancePatterns, body)

    private fun firstMatch(patterns: List<Regex>, body: String): Double? =
        patterns.firstNotNullOfOrNull { p ->
            p.find(body)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
        }
}
