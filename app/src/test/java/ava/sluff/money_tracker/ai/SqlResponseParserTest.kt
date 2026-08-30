package ava.sluff.money_tracker.ai

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SqlResponseParserTest {

    private val parser = SqlResponseParser(Gson())

    @Test
    fun `reads bare json`() {
        val plan = parser.parse("""{"sql":"SELECT 1"}""")
        assertEquals("SELECT 1", plan?.sql)
    }

    @Test
    fun `reads json wrapped in a fenced block`() {
        val plan = parser.parse("```json\n{\"sql\":\"SELECT 1\"}\n```")
        assertEquals("SELECT 1", plan?.sql)
    }

    @Test
    fun `reads json that follows explanatory prose`() {
        val plan = parser.parse("Sure, here is the query:\n{\"sql\":\"SELECT 2\"}")
        assertEquals("SELECT 2", plan?.sql)
    }

    @Test
    fun `reads a direct answer that needs no query`() {
        val plan = parser.parse("""{"answer":"I can total your spending by category."}""")
        assertNull(plan?.sql)
        assertEquals("I can total your spending by category.", plan?.answer)
    }

    @Test
    fun `treats a blank sql string as absent`() {
        val plan = parser.parse("""{"sql":"   ","answer":"nothing to run"}""")
        assertNull(plan?.sql)
    }

    @Test
    fun `returns null when the response is not json`() {
        assertNull(parser.parse("I am sorry, I cannot help with that."))
    }

    @Test
    fun `returns null when the json has neither field`() {
        assertNull(parser.parse("""{"thoughts":"hmm"}"""))
    }
}
