package ava.sluff.money_tracker.sms

object SmsParser {

    private val bankPatterns = listOf(
        Regex("(?i)(debited|credited|debit|credit|spent|received|withdrawn|transferred|payment|txn|transaction|حوالة|تحويل|سحب|إيداع|دفع|شراء|صادرة|واردة)"),
        Regex("(?i)(a/c|acct|account|card|حساب|بطاقة).*\\d{3,}"),
        Regex("(?i)(rs\\.?|inr|usd|eur|gbp|jod|jd|\\$|₹|€|£|دينار|ريال|درهم|ليرة|جنيه|مبلغ)\\s*[\\d,.]+"),
        Regex("\\d+[.,]\\d{2,3}"),
        Regex("(?i)(bal|balance|avl\\.?|available|رصيد|المتوفر|المتبقي).*\\d")
    )

    /**
     * Markers of a one-time passcode rather than a completed transaction.
     *
     * These messages quote the purchase amount and currency, so they satisfy the generic bank
     * patterns above, but no money has moved yet: the real debit arrives separately moments
     * later. Recording both counted the same purchase twice, and because the passcode states
     * the merchant's currency, a foreign amount was being stored as if it were local.
     */
    private val authorizationCodePatterns = listOf(
        Regex("(?i)code to complete"),
        Regex("(?i)(verification|one[- ]time|otp)\\s*(code|password|pin)"),
        Regex("(?i)\\b(otp|3d ?secure)\\b"),
        Regex("(?i)(don'?t|do not|لا)\\s*(share|تشارك)"),
        Regex("رمز التحقق|رمز التأكيد|كلمة المرور لمرة واحدة")
    )

    fun isAuthorizationCode(body: String): Boolean =
        authorizationCodePatterns.any { it.containsMatchIn(body) }

    fun isBankSms(sender: String, body: String): Boolean =
        !isAuthorizationCode(body) && bankPatterns.count { it.containsMatchIn(body) } >= 2
}
