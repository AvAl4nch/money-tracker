package ava.sluff.money_tracker.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueryResultTest {

    @Test
    fun `renders a header row followed by the data`() {
        val result = QueryResult(
            columns = listOf("name", "total"),
            rows = listOf(listOf("Health", "118.3"), listOf("Education", "1749.0")),
            truncated = false
        )

        assertEquals("name,total\nHealth,118.3\nEducation,1749.0", result.toCsv())
    }

    @Test
    fun `says so when the query returned nothing`() {
        val result = QueryResult(columns = listOf("total"), rows = emptyList(), truncated = false)

        assertTrue(result.toCsv().contains("no rows", ignoreCase = true))
    }

    @Test
    fun `quotes a value containing a comma so columns stay aligned`() {
        val result = QueryResult(
            columns = listOf("merchant"),
            rows = listOf(listOf("SMITH, JOHN")),
            truncated = false
        )

        assertEquals("merchant\n\"SMITH, JOHN\"", result.toCsv())
    }

    @Test
    fun `marks a truncated result so the model does not overstate the answer`() {
        val result = QueryResult(
            columns = listOf("id"),
            rows = listOf(listOf("1")),
            truncated = true
        )

        assertTrue(result.toCsv().contains("truncated", ignoreCase = true))
    }

    @Test
    fun `stops adding rows once the character budget is spent`() {
        val rows = (1..500).map { listOf("row$it") }
        val result = QueryResult(columns = listOf("v"), rows = rows, truncated = false)

        val csv = result.toCsv(maxChars = 100)

        assertTrue(csv.length < 200)
        assertTrue(csv.contains("truncated", ignoreCase = true))
    }

    @Test
    fun `renders a null value as an empty field`() {
        val result = QueryResult(columns = listOf("note"), rows = listOf(listOf("")), truncated = false)

        assertEquals("note\n", result.toCsv())
    }
}
