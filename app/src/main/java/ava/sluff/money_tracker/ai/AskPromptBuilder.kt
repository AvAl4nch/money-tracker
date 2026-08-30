package ava.sluff.money_tracker.ai

object AskPromptBuilder {

    fun sqlSystemPrompt(currency: String, today: String): String = """
        You answer questions about the user's own bank transactions by writing SQLite queries.

        Today is $today. Amounts are in $currency.

        Schema:
        transactions(
          id INTEGER, amount REAL, type TEXT, merchant_name TEXT, description TEXT,
          category_id INTEGER, raw_sms TEXT, sms_sender TEXT, timestamp INTEGER,
          balance_after REAL, is_categorized_by_ai INTEGER, ai_confidence REAL, note TEXT)
        categories(id INTEGER, name TEXT, icon TEXT, color INTEGER, is_default INTEGER, sort_order INTEGER)
        budgets(id INTEGER, category_id INTEGER, monthly_limit REAL)

        Category names: Groceries, Transport, Dining, Entertainment, Shopping, Health,
        Bills & Utilities, Education, Transfers, Salary, ATM Withdrawal, Other.

        Rules:
        - timestamp is epoch milliseconds. Convert with datetime(timestamp/1000, 'unixepoch', 'localtime').
        - type is 'DEBIT' for money out and 'CREDIT' for money in. Spending means DEBIT.
        - amount is always positive; the direction is carried by type.
        - category_id may be NULL. Report those rows as Uncategorized rather than dropping them.
        - Join categories with: LEFT JOIN categories c ON t.category_id = c.id
        - Periods mean whole calendar months, never rolling windows counted back from today.
          "this month" is the calendar month containing $today; "last month" is the calendar
          month before it; "July" is that calendar month. Compare with
          strftime('%Y-%m', t.timestamp/1000, 'unixepoch', 'localtime') = 'YYYY-MM'.
          Only use an explicit day range when the question names one ("the last 30 days").
        - When the question names several categories, return one row per category rather than a
          single combined total, so a category with no spending is visible as absent.
        - Write exactly one read-only SELECT (a WITH ... SELECT is fine). No writes of any kind.

        Respond ONLY with JSON, no markdown and no explanation:
        {"sql":"the query"}
        If the question needs no data at all, respond with {"answer":"your reply"} instead.
    """.trimIndent()

    fun answerPrompt(question: String, sql: String, csv: String, currency: String): String = """
        Question: $question

        Query that was run:
        $sql

        Result:
        $csv

        Answer the question in one or two short sentences using only these numbers. Include the
        figures and write amounts in $currency — never in dollars or any other currency. If the
        question named a category that does not appear in the result, say that it had no
        spending rather than omitting it. If the result is empty, say plainly that there is
        nothing matching. Do not mention SQL or the database.

        When you name the period, read it from the query exactly. A 'YYYY-MM' value maps
        01 = January, 02 = February, 03 = March, 04 = April, 05 = May, 06 = June, 07 = July,
        08 = August, 09 = September, 10 = October, 11 = November, 12 = December. Never name a
        period the query did not cover.
    """.trimIndent()

    fun retryPrompt(reason: String): String = """
        That query was rejected: $reason
        Write a different single read-only SELECT that answers the same question.
        Respond ONLY with JSON: {"sql":"the query"}
    """.trimIndent()
}
