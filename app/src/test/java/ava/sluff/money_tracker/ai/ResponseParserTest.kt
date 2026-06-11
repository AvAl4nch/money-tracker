package ava.sluff.money_tracker.ai

import ava.sluff.money_tracker.domain.model.TransactionType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class ResponseParserTest {

    private val parser = ResponseParser(Gson())

    @Test
    fun `parses plain json response`() {
        val json = """{"type":"DEBIT","amount":15.276,"merchant":"STEAM","description":"Game purchase","category":"Entertainment","confidence":0.95,"balance_after":100.5}"""
        val result = parser.parse(json)
        assertNotNull(result)
        assertEquals(TransactionType.DEBIT, result!!.type)
        assertEquals(15.276, result.amount, 0.0001)
        assertEquals("STEAM", result.merchantName)
        assertEquals("Entertainment", result.categoryName)
        assertEquals(0.95f, result.confidence, 0.0001f)
        assertEquals(100.5, result.balanceAfter!!, 0.0001)
    }

    @Test
    fun `strips markdown fences`() {
        val fenced = "```json\n{\"type\":\"CREDIT\",\"amount\":693.75,\"merchant\":null,\"description\":\"Salary\",\"category\":\"Salary\",\"confidence\":1.0,\"balance_after\":null}\n```"
        val result = parser.parse(fenced)
        assertNotNull(result)
        assertEquals(TransactionType.CREDIT, result!!.type)
        assertNull(result.merchantName)
        assertNull(result.balanceAfter)
    }

    @Test
    fun `returns null for garbage`() {
        assertNull(parser.parse("I cannot categorize this message."))
    }

    @Test
    fun `returns null when amount missing`() {
        assertNull(parser.parse("""{"type":"DEBIT","category":"Other","confidence":0.2}"""))
    }

    @Test
    fun `unknown type defaults to DEBIT`() {
        val json = """{"type":"WAT","amount":1.0,"merchant":null,"description":null,"category":"Other","confidence":0.1,"balance_after":null}"""
        assertEquals(TransactionType.DEBIT, parser.parse(json)!!.type)
    }

    @Test
    fun `confidence clamped to 0-1`() {
        val json = """{"type":"DEBIT","amount":1.0,"merchant":null,"description":null,"category":"Other","confidence":3.7,"balance_after":null}"""
        assertEquals(1.0f, parser.parse(json)!!.confidence, 0.0001f)
    }

    @Test
    fun `extracts json despite leading prose`() {
        val noisy = "Here is the categorization you asked for:\n```json\n{\"type\":\"DEBIT\",\"amount\":2.5,\"merchant\":\"QAHWA BLK\",\"description\":\"Coffee\",\"category\":\"Dining\",\"confidence\":0.9,\"balance_after\":null}\n``` Hope that helps!"
        val result = parser.parse(noisy)
        assertNotNull(result)
        assertEquals("Dining", result!!.categoryName)
    }
}
