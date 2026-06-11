package ava.sluff.money_tracker.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsAmountExtractorTest {

    @Test
    fun `extracts amount from authorization sms`() {
        val body = "تفويض حركة على حسابكم 1234567 - 001 بقيمة 20.15 دينار اردني من JO-PETROL الرصيد المتوفر 3367.185 دينار اردني"
        assertEquals(20.15, SmsAmountExtractor.amount(body)!!, 0.0001)
        assertEquals(3367.185, SmsAmountExtractor.balanceAfter(body)!!, 0.0001)
    }

    @Test
    fun `extracts amount from cliq transfer sms`() {
        val body = "حوالة كليك صادرة بمبلغ 29.5 دينار اردني من حسابكم 1234567 - 001 الى SOMEONE الرصيد المتوفر 3394.935 دينار اردني"
        assertEquals(29.5, SmsAmountExtractor.amount(body)!!, 0.0001)
    }

    @Test
    fun `extracts amount from atm sms with new balance keyword`() {
        val body = "حركة صراف آلي على حسابكم 1234567 - 001 بقيمة 10.0 دينار اردني بتاريخ 07062026 ليصبح رصيدكم 3312.752 دينار اردني"
        assertEquals(10.0, SmsAmountExtractor.amount(body)!!, 0.0001)
        assertEquals(3312.752, SmsAmountExtractor.balanceAfter(body)!!, 0.0001)
    }

    @Test
    fun `extracts amount from english sms`() {
        val body = "Your account 1234 was debited JOD 15.276 at STEAM. Available balance 693.750"
        assertEquals(15.276, SmsAmountExtractor.amount(body)!!, 0.0001)
    }

    @Test
    fun `returns null when nothing numeric`() {
        assertNull(SmsAmountExtractor.amount("hello there"))
        assertNull(SmsAmountExtractor.balanceAfter("hello there"))
    }
}
