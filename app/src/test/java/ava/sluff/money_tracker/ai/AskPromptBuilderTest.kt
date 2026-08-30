package ava.sluff.money_tracker.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AskPromptBuilderTest {

    @Test
    fun `system prompt names every table the model may query`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("transactions"))
        assertTrue(prompt.contains("categories"))
        assertTrue(prompt.contains("budgets"))
    }

    @Test
    fun `system prompt explains that timestamps are epoch milliseconds`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("timestamp/1000"))
        assertTrue(prompt.contains("unixepoch"))
    }

    @Test
    fun `system prompt carries todays date so relative questions resolve`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("2026-08-30"))
    }

    @Test
    fun `system prompt states that spending means DEBIT`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("DEBIT"))
        assertTrue(prompt.contains("CREDIT"))
    }

    @Test
    fun `system prompt demands a read-only statement`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("SELECT"))
    }

    @Test
    fun `system prompt defines last month as the previous calendar month`() {
        val prompt = AskPromptBuilder.sqlSystemPrompt(currency = "JOD", today = "2026-08-30")

        assertTrue(prompt.contains("calendar"))
        assertTrue(prompt.contains("strftime('%Y-%m'"))
    }

    @Test
    fun `answer prompt carries the question, the query and the rows`() {
        val prompt = AskPromptBuilder.answerPrompt(
            question = "how much on health?",
            sql = "SELECT SUM(amount) FROM transactions",
            csv = "total\n118.3",
            currency = "JOD"
        )

        assertTrue(prompt.contains("how much on health?"))
        assertTrue(prompt.contains("SELECT SUM(amount) FROM transactions"))
        assertTrue(prompt.contains("118.3"))
    }

    @Test
    fun `answer prompt names the currency so the reply does not invent one`() {
        val prompt = AskPromptBuilder.answerPrompt(
            question = "how much?",
            sql = "SELECT 1",
            csv = "total\n1225.0",
            currency = "JOD"
        )

        assertTrue(prompt.contains("JOD"))
    }

    @Test
    fun `answer prompt explains how to read a YYYY-MM month so it is not off by one`() {
        val prompt = AskPromptBuilder.answerPrompt(
            question = "and the month before?",
            sql = "SELECT 1 WHERE strftime('%Y-%m', x) = '2026-06'",
            csv = "total\n3.75",
            currency = "JOD"
        )

        assertTrue(prompt.contains("01 = January"))
        assertTrue(prompt.contains("06 = June"))
    }

    @Test
    fun `retry prompt repeats the rejection reason`() {
        val prompt = AskPromptBuilder.retryPrompt("DELETE is not allowed")

        assertTrue(prompt.contains("DELETE is not allowed"))
    }
}
