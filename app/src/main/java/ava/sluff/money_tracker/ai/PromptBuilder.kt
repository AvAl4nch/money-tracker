package ava.sluff.money_tracker.ai

object PromptBuilder {

    private val categories = listOf(
        "Groceries", "Transport", "Dining", "Entertainment", "Shopping", "Health",
        "Bills & Utilities", "Education", "Transfers", "Salary", "ATM Withdrawal", "Other"
    )

    fun systemPrompt(currency: String): String = """
        You are a bank transaction categorizer. Given a bank SMS message, extract transaction details and categorize it.

        Available categories: ${categories.joinToString(", ")}

        Respond ONLY with valid JSON (no markdown, no explanation):
        {"type":"DEBIT or CREDIT","amount":number,"merchant":"merchant name or null","description":"short human-readable description of this transaction","category":"one of the categories above","confidence":0.0 to 1.0,"balance_after":number or null}

        Rules:
        - type: DEBIT for outgoing money, CREDIT for incoming money
        - description: brief summary like "CliQ transfer to Ahmad" or "ATM withdrawal" or "Grocery purchase at Carrefour"
        - Currency context: $currency
        - If you cannot determine a field, use null.
        - If unsure about category, use "Other" with low confidence.
    """.trimIndent()

    fun userPrompt(smsBody: String): String = "Categorize this bank SMS:\n$smsBody"
}
