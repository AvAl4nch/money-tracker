package ava.sluff.money_tracker.data.repository

/**
 * The rows a generated query produced, rendered for the model rather than for the screen.
 *
 * [truncated] is carried all the way into the CSV so the model can qualify its answer instead
 * of presenting a partial total as a complete one.
 */
data class QueryResult(
    val columns: List<String>,
    val rows: List<List<String>>,
    val truncated: Boolean
) {

    fun toCsv(maxChars: Int = MAX_CHARS): String {
        if (rows.isEmpty()) return "(no rows)"

        val builder = StringBuilder(columns.joinToString(","))
        var cut = false
        for (row in rows) {
            val line = "\n" + row.joinToString(",") { escape(it) }
            if (builder.length + line.length > maxChars) {
                cut = true
                break
            }
            builder.append(line)
        }
        if (truncated || cut) {
            builder.append("\n(results truncated — this is not the complete set of rows)")
        }
        return builder.toString()
    }

    private fun escape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    companion object {
        const val MAX_CHARS = 8000
    }
}
