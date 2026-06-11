package ava.sluff.money_tracker.sms

object SmsParser {

    private val bankPatterns = listOf(
        Regex("(?i)(debited|credited|debit|credit|spent|received|withdrawn|transferred|payment|txn|transaction|حوالة|تحويل|سحب|إيداع|دفع|شراء|صادرة|واردة)"),
        Regex("(?i)(a/c|acct|account|card|حساب|بطاقة).*\\d{3,}"),
        Regex("(?i)(rs\\.?|inr|usd|eur|gbp|jod|jd|\\$|₹|€|£|دينار|ريال|درهم|ليرة|جنيه|مبلغ)\\s*[\\d,.]+"),
        Regex("\\d+[.,]\\d{2,3}"),
        Regex("(?i)(bal|balance|avl\\.?|available|رصيد|المتوفر|المتبقي).*\\d")
    )

    fun isBankSms(sender: String, body: String): Boolean =
        bankPatterns.count { it.containsMatchIn(body) } >= 2
}
