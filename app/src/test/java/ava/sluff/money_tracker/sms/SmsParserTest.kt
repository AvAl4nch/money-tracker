package ava.sluff.money_tracker.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {

    @Test
    fun `english debit sms with amount and balance is bank sms`() {
        val body = "Your account 1234 was debited JOD 15.276 at STEAM. Available balance 693.750"
        assertTrue(SmsParser.isBankSms("JIB", body))
    }

    @Test
    fun `real jordanian bank authorization sms is bank sms`() {
        // Real format from the user's bank (digits sanitized): no keyword from
        // pattern 1 and currency follows the amount, so detection relies on
        // patterns 2 (account+digits), 4 (decimal amount), 5 (balance keywords).
        val body = "تفويض حركة على حسابكم 1234567 - 001 بقيمة 0.7 دينار اردني من C TOWN AMMAN MALL        AMMAN        JO الرصيد المتوفر 1234.567 دينار اردني"
        assertTrue(SmsParser.isBankSms("JIB", body))
    }

    @Test
    fun `arabic transfer sms is bank sms`() {
        val body = "تم تحويل مبلغ 2.100 دينار من حساب 5678 الرصيد المتوفر 100.500"
        assertTrue(SmsParser.isBankSms("JIB", body))
    }

    @Test
    fun `otp sms is not bank sms`() {
        val body = "Your OTP code is 482913. Do not share it with anyone."
        assertFalse(SmsParser.isBankSms("JIB", body))
    }

    @Test
    fun `plain conversation sms is not bank sms`() {
        val body = "hey are we still meeting tomorrow?"
        assertFalse(SmsParser.isBankSms("+962790000000", body))
    }

    @Test
    fun `single pattern match alone is not enough`() {
        // matches only the decimal-amount pattern, nothing else
        val body = "version 2.50 released"
        assertFalse(SmsParser.isBankSms("VENDOR", body))
    }
}
