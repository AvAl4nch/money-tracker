package ava.sluff.money_tracker.ai

import ava.sluff.money_tracker.data.remote.Message
import ava.sluff.money_tracker.data.repository.QueryResult
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataQueryAgentTest {

    private class FakeLlm(private val replies: MutableList<String?>) : LlmClient {
        val prompts = mutableListOf<List<Message>>()
        override suspend fun complete(messages: List<Message>, temperature: Double): String? {
            prompts += messages
            return if (replies.isEmpty()) null else replies.removeAt(0)
        }
    }

    private class FakeRunner(private val result: QueryResult) : QueryRunner {
        val executed = mutableListOf<String>()
        override suspend fun run(sql: String): QueryResult {
            executed += sql
            return result
        }
    }

    private val rows = QueryResult(listOf("total"), listOf(listOf("118.3")), truncated = false)

    private fun agent(llm: LlmClient, runner: QueryRunner) =
        DataQueryAgent(llm, runner, SqlGuard(), SqlResponseParser(Gson()))

    @Test
    fun `answers a question by running the generated query`() = runTest {
        val llm = FakeLlm(
            mutableListOf("""{"sql":"SELECT SUM(amount) FROM transactions"}""", "You spent 118.3.")
        )
        val runner = FakeRunner(rows)

        val result = agent(llm, runner).ask("how much on health?", emptyList(), "JOD", "2026-08-30")

        assertEquals(
            AskResult.Answer("You spent 118.3.", "SELECT SUM(amount) FROM transactions LIMIT 200"),
            result
        )
        assertEquals(1, runner.executed.size)
    }

    @Test
    fun `returns the direct answer when the model asks for no data`() = runTest {
        val llm = FakeLlm(mutableListOf("""{"answer":"Ask me about a category or a month."}"""))
        val runner = FakeRunner(rows)

        val result = agent(llm, runner).ask("hello", emptyList(), "JOD", "2026-08-30")

        assertEquals(AskResult.Answer("Ask me about a category or a month.", null), result)
        assertTrue(runner.executed.isEmpty())
    }

    @Test
    fun `retries once when the guard rejects the first query`() = runTest {
        val llm = FakeLlm(
            mutableListOf(
                """{"sql":"DELETE FROM transactions"}""",
                """{"sql":"SELECT SUM(amount) FROM transactions"}""",
                "You spent 118.3."
            )
        )
        val runner = FakeRunner(rows)

        val result = agent(llm, runner).ask("how much?", emptyList(), "JOD", "2026-08-30")

        assertTrue(result is AskResult.Answer)
        assertEquals(1, runner.executed.size)
    }

    @Test
    fun `retries once when the query fails to execute`() = runTest {
        val llm = FakeLlm(
            mutableListOf(
                """{"sql":"SELECT nope FROM transactions"}""",
                """{"sql":"SELECT SUM(amount) FROM transactions"}""",
                "You spent 118.3."
            )
        )
        val runner = object : QueryRunner {
            val executed = mutableListOf<String>()
            private var first = true
            override suspend fun run(sql: String): QueryResult {
                executed += sql
                if (first) {
                    first = false
                    throw IllegalStateException("no such column: nope")
                }
                return rows
            }
        }

        val result = agent(llm, runner).ask("how much?", emptyList(), "JOD", "2026-08-30")

        assertTrue(result is AskResult.Answer)
        assertEquals(2, runner.executed.size)
    }

    @Test
    fun `gives up after a second rejection`() = runTest {
        val llm = FakeLlm(
            mutableListOf(
                """{"sql":"DELETE FROM transactions"}""",
                """{"sql":"DROP TABLE transactions"}"""
            )
        )
        val runner = FakeRunner(rows)

        val result = agent(llm, runner).ask("wipe it", emptyList(), "JOD", "2026-08-30")

        assertTrue(result is AskResult.CouldNotAnswer)
        assertTrue(runner.executed.isEmpty())
    }

    @Test
    fun `reports a network failure when the model cannot be reached`() = runTest {
        val llm = FakeLlm(mutableListOf(null))

        val result = agent(llm, FakeRunner(rows)).ask("how much?", emptyList(), "JOD", "2026-08-30")

        assertEquals(AskResult.NetworkError, result)
    }

    @Test
    fun `passes prior turns to the model so follow-ups keep context`() = runTest {
        val llm = FakeLlm(mutableListOf("""{"answer":"ok"}"""))
        val history = listOf(Message("user", "how much on health?"), Message("assistant", "118.3"))

        agent(llm, FakeRunner(rows)).ask("what about last month?", history, "JOD", "2026-08-30")

        val sent = llm.prompts.first().map { it.content }
        assertTrue(sent.any { it.contains("how much on health?") })
    }
}
