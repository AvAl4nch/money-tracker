package ava.sluff.money_tracker.ai

import javax.inject.Inject
import javax.inject.Singleton

sealed interface GuardResult {
    data class Allowed(val sql: String) : GuardResult
    data class Rejected(val reason: String) : GuardResult
}

/**
 * Decides whether a model-generated statement is safe to run against the user's database.
 *
 * This is a security boundary, so it fails closed: anything it cannot confidently classify as
 * a single read-only statement is rejected. Rejection is routine — the caller feeds the reason
 * back to the model and asks for another attempt.
 */
@Singleton
class SqlGuard @Inject constructor() {

    fun validate(sql: String): GuardResult {
        if (sql.length > MAX_LENGTH) {
            return GuardResult.Rejected("Query is longer than $MAX_LENGTH characters.")
        }

        val stripped = stripComments(sql).trim().trimEnd(';').trim()
        if (stripped.isBlank()) return GuardResult.Rejected("Query is empty.")

        val outsideLiterals = blankOutStringLiterals(stripped)

        if (outsideLiterals.contains(';')) {
            return GuardResult.Rejected("Only one statement is allowed.")
        }

        val upper = outsideLiterals.uppercase()
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            return GuardResult.Rejected("Query must start with SELECT or WITH.")
        }

        FORBIDDEN.firstOrNull { Regex("\\b$it\\b").containsMatchIn(upper) }?.let {
            return GuardResult.Rejected("$it is not allowed; only read-only queries can run.")
        }

        val limited = if (Regex("(?i)\\bLIMIT\\b").containsMatchIn(outsideLiterals)) {
            stripped
        } else {
            "$stripped LIMIT $ROW_LIMIT"
        }
        return GuardResult.Allowed(limited)
    }

    /** Removes `--` line comments and block comments so keywords cannot hide inside them. */
    private fun stripComments(sql: String): String {
        val withoutBlocks = sql.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), " ")
        return withoutBlocks.replace(Regex("--[^\n]*"), " ")
    }

    /**
     * Replaces the contents of single-quoted literals with spaces so that a merchant called
     * "UPDATE CAFE" is not mistaken for a write, while keeping the string's length and quotes.
     */
    private fun blankOutStringLiterals(sql: String): String {
        val out = StringBuilder(sql.length)
        var inLiteral = false
        for (c in sql) {
            when {
                c == '\'' -> {
                    inLiteral = !inLiteral
                    out.append(c)
                }
                inLiteral -> out.append(' ')
                else -> out.append(c)
            }
        }
        return out.toString()
    }

    companion object {
        const val ROW_LIMIT = 200
        private const val MAX_LENGTH = 4000
        private val FORBIDDEN = listOf(
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "CREATE", "REPLACE",
            "ATTACH", "DETACH", "PRAGMA", "VACUUM", "REINDEX", "TRIGGER"
        )
    }
}
