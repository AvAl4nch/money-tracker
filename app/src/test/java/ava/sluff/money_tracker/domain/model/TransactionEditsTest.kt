package ava.sluff.money_tracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionEditsTest {

    private val original = Transaction(
        id = 42L,
        amount = 12.500,
        type = TransactionType.DEBIT,
        merchantName = "CARREFOUR",
        description = "Grocery purchase",
        categoryId = 1L,
        categoryName = "Groceries",
        categoryIcon = "shopping_cart",
        categoryColor = 4283215696L,
        rawSms = "Purchase of JOD 12.500 at CARREFOUR. Balance: JOD 300.000",
        smsSender = "ARABBANK",
        timestamp = 1_750_000_000_000L,
        balanceAfter = 300.0,
        isCategorizedByAi = true,
        aiConfidence = 0.92f,
        note = null
    )

    private val edits = TransactionEdits(
        amount = 15.750,
        type = TransactionType.CREDIT,
        merchantName = "SAFEWAY",
        description = "Refund",
        categoryId = 5L,
        note = "returned the blender",
        timestamp = 1_750_500_000_000L
    )

    @Test
    fun `applies every edited field to the entity`() {
        val entity = original.applyEdits(edits)

        assertEquals(15.750, entity.amount, 0.0001)
        assertEquals("CREDIT", entity.type)
        assertEquals("SAFEWAY", entity.merchantName)
        assertEquals("Refund", entity.description)
        assertEquals(5L, entity.categoryId)
        assertEquals("returned the blender", entity.note)
        assertEquals(1_750_500_000_000L, entity.timestamp)
    }

    @Test
    fun `preserves the original SMS as the source of truth`() {
        val entity = original.applyEdits(edits)

        assertEquals(original.rawSms, entity.rawSms)
        assertEquals(original.smsSender, entity.smsSender)
    }

    @Test
    fun `keeps the row identity so the edit updates rather than inserts`() {
        val entity = original.applyEdits(edits)

        assertEquals(42L, entity.id)
    }

    @Test
    fun `clears the AI-categorized flag because a human decided this row`() {
        val entity = original.applyEdits(edits)

        assertFalse(entity.isCategorizedByAi)
    }

    @Test
    fun `keeps the recorded AI confidence as history`() {
        val entity = original.applyEdits(edits)

        assertEquals(0.92f, entity.aiConfidence!!, 0.0001f)
    }

    @Test
    fun `keeps the balance reported by the bank because the user cannot edit it`() {
        val entity = original.applyEdits(edits)

        assertEquals(300.0, entity.balanceAfter!!, 0.0001)
    }

    @Test
    fun `stores a blank merchant as null rather than an empty string`() {
        val entity = original.applyEdits(edits.copy(merchantName = "   "))

        assertNull(entity.merchantName)
    }

    @Test
    fun `stores a blank note as null rather than an empty string`() {
        val entity = original.applyEdits(edits.copy(note = ""))

        assertNull(entity.note)
    }

    @Test
    fun `allows clearing the category back to uncategorized`() {
        val entity = original.applyEdits(edits.copy(categoryId = null))

        assertNull(entity.categoryId)
    }
}
