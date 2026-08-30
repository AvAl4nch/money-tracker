package ava.sluff.money_tracker.ai

import ava.sluff.money_tracker.data.remote.Message
import ava.sluff.money_tracker.data.repository.QueryResult
import javax.inject.Inject

/** The model call, narrowed to what the agent needs so it can be faked in tests. */
interface LlmClient {
    suspend fun complete(messages: List<Message>, temperature: Double): String?
}

/** Executing an already-guarded statement, narrowed for the same reason. */
interface QueryRunner {
    suspend fun run(sql: String): QueryResult
}

sealed interface AskResult {
    data class Answer(val text: String, val sql: String?) : AskResult
    data object NeedsConfiguration : AskResult
    data object NetworkError : AskResult
    data class CouldNotAnswer(val reason: String) : AskResult
}

/**
 * Turns a plain-language question into an answer computed from the user's own records.
 *
 * The model proposes a query, [SqlGuard] decides whether it may run, and the rows come back to
 * the model to be phrased. A rejected or broken query is reported back to the model once, which
 * recovers from the usual mistakes — a wrong column name, a forbidden statement — without
 * looping indefinitely.
 */
class DataQueryAgent @Inject constructor(
    private val llm: LlmClient,
    private val runner: QueryRunner,
    private val guard: SqlGuard,
    private val parser: SqlResponseParser
) {

    suspend fun ask(
        question: String,
        history: List<Message>,
        currency: String,
        today: String
    ): AskResult {
        val conversation = mutableListOf(
            Message("system", AskPromptBuilder.sqlSystemPrompt(currency, today))
        )
        conversation += history
        conversation += Message("user", question)

        var attempt = 0
        while (attempt < MAX_ATTEMPTS) {
            val raw = llm.complete(conversation, SQL_TEMPERATURE) ?: return AskResult.NetworkError
            val plan = parser.parse(raw)
                ?: return AskResult.CouldNotAnswer("The model did not return a usable query.")

            if (plan.sql == null) {
                return plan.answer?.let { AskResult.Answer(it, null) }
                    ?: AskResult.CouldNotAnswer("The model returned no query.")
            }

            val failure = when (val verdict = guard.validate(plan.sql)) {
                is GuardResult.Rejected -> verdict.reason
                is GuardResult.Allowed -> {
                    val rows = try {
                        runner.run(verdict.sql)
                    } catch (e: Exception) {
                        null
                    }
                    if (rows != null) return phrase(question, verdict.sql, rows, currency)
                    "The query could not be executed against the database."
                }
            }

            conversation += Message("assistant", raw)
            conversation += Message("user", AskPromptBuilder.retryPrompt(failure))
            attempt++
        }
        return AskResult.CouldNotAnswer("I could not turn that into a query I am allowed to run.")
    }

    private suspend fun phrase(
        question: String,
        sql: String,
        rows: QueryResult,
        currency: String
    ): AskResult {
        val text = llm.complete(
            listOf(Message("user", AskPromptBuilder.answerPrompt(question, sql, rows.toCsv(), currency))),
            ANSWER_TEMPERATURE
        ) ?: return AskResult.NetworkError
        return AskResult.Answer(text.trim(), sql)
    }

    companion object {
        private const val MAX_ATTEMPTS = 2
        private const val SQL_TEMPERATURE = 0.0
        private const val ANSWER_TEMPERATURE = 0.3
    }
}
