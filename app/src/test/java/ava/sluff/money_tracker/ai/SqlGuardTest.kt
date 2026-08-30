package ava.sluff.money_tracker.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SqlGuardTest {

    private val guard = SqlGuard()

    private fun allowed(sql: String): String {
        val result = guard.validate(sql)
        assertTrue("expected Allowed, got $result", result is GuardResult.Allowed)
        return (result as GuardResult.Allowed).sql
    }

    private fun rejected(sql: String): String {
        val result = guard.validate(sql)
        assertTrue("expected Rejected, got $result", result is GuardResult.Rejected)
        return (result as GuardResult.Rejected).reason
    }

    @Test
    fun `allows a plain select`() {
        assertEquals(
            "SELECT SUM(amount) FROM transactions LIMIT 200",
            allowed("SELECT SUM(amount) FROM transactions")
        )
    }

    @Test
    fun `allows a common table expression`() {
        assertTrue(allowed("WITH m AS (SELECT 1 AS n) SELECT n FROM m").startsWith("WITH"))
    }

    @Test
    fun `allows a subquery containing the word select`() {
        val sql = "SELECT * FROM transactions WHERE amount > (SELECT AVG(amount) FROM transactions)"
        assertTrue(allowed(sql).startsWith("SELECT"))
    }

    @Test
    fun `keeps an existing limit instead of adding a second one`() {
        val sql = allowed("SELECT id FROM transactions LIMIT 5")
        assertEquals(1, Regex("(?i)\\bLIMIT\\b").findAll(sql).count())
    }

    @Test
    fun `strips a trailing semicolon`() {
        assertEquals("SELECT 1 LIMIT 200", allowed("SELECT 1;"))
    }

    @Test
    fun `rejects a delete`() {
        assertTrue(rejected("DELETE FROM transactions").isNotBlank())
    }

    @Test
    fun `rejects an update`() {
        assertTrue(rejected("UPDATE transactions SET amount = 0").isNotBlank())
    }

    @Test
    fun `rejects a drop`() {
        assertTrue(rejected("DROP TABLE transactions").isNotBlank())
    }

    @Test
    fun `rejects a write stacked after a select`() {
        assertTrue(rejected("SELECT 1; DELETE FROM transactions").isNotBlank())
    }

    @Test
    fun `rejects a write hidden behind a line comment`() {
        assertTrue(rejected("SELECT 1 -- harmless\n; DROP TABLE transactions").isNotBlank())
    }

    @Test
    fun `rejects a write hidden inside a block comment boundary`() {
        assertTrue(rejected("SELECT 1 /* note */ ; UPDATE transactions SET amount = 0").isNotBlank())
    }

    @Test
    fun `rejects pragma`() {
        assertTrue(rejected("PRAGMA table_info(transactions)").isNotBlank())
    }

    @Test
    fun `rejects attach`() {
        assertTrue(rejected("SELECT 1 UNION SELECT 2; ATTACH DATABASE 'x' AS y").isNotBlank())
    }

    @Test
    fun `rejects a blank statement`() {
        assertTrue(rejected("   ").isNotBlank())
    }

    @Test
    fun `rejects a statement longer than the cap`() {
        assertTrue(rejected("SELECT " + "a".repeat(4100)).isNotBlank())
    }

    @Test
    fun `does not reject a merchant name that contains a keyword`() {
        val sql = allowed("SELECT * FROM transactions WHERE merchant_name = 'UPDATE CAFE'")
        assertTrue(sql.contains("UPDATE CAFE"))
    }
}
