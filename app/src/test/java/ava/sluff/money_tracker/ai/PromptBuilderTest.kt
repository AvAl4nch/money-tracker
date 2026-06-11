package ava.sluff.money_tracker.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun `system prompt contains all 12 categories and currency`() {
        val prompt = PromptBuilder.systemPrompt("JOD")
        listOf(
            "Groceries", "Transport", "Dining", "Entertainment", "Shopping", "Health",
            "Bills & Utilities", "Education", "Transfers", "Salary", "ATM Withdrawal", "Other"
        ).forEach { assertTrue("missing $it", prompt.contains(it)) }
        assertTrue(prompt.contains("Currency context: JOD"))
        assertTrue(prompt.contains("Respond ONLY with valid JSON"))
    }

    @Test
    fun `user prompt wraps sms body`() {
        assertEquals(
            "Categorize this bank SMS:\nsome sms",
            PromptBuilder.userPrompt("some sms")
        )
    }
}
