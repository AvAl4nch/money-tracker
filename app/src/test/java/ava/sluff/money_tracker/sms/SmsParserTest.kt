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
    fun `authorization code sms carrying an amount is not bank sms`() {
        // Real message from the user's bank. It states an amount and a currency, so it passes
        // the generic pattern count, but it is a one-time passcode: the actual debit arrives
        // separately seconds later and would otherwise be counted twice.
        val body = "Your code to complete your transaction from MERCHANT WEBSITE AND SELFCARE APP " +
            "with amount 1.160 JOD is 421302, please don\u2019t share it with others"
        assertFalse(SmsParser.isBankSms("IslamicBank", body))
    }

    @Test
    fun `foreign currency authorization code sms is not bank sms`() {
        val body = "Your code to complete your transaction from MERCHANT* with amount 10.99 EUR " +
            "is 304122, please don\u2019t share it with others"
        assertFalse(SmsParser.isBankSms("IslamicBank", body))
    }

    @Test
    fun `authorization code sms with a grouped amount is not bank sms`() {
        val body = "Your code to complete your transaction from MERCHANT with amount 1,749.00 USD " +
            "is 741816, please don\u2019t share it with others"
        assertFalse(SmsParser.isBankSms("IslamicBank", body))
    }

    @Test
    fun `arabic verification code sms is not bank sms`() {
        val body = "رمز التحقق الخاص بك هو 482913 لإتمام عملية شراء بمبلغ 25.500 دينار اردني"
        assertFalse(SmsParser.isBankSms("IslamicBank", body))
    }

    @Test
    fun `a real debit sms is still recognised`() {
        // Guards the filter above from swallowing genuine debits.
        val body = "تفويض حركة على حسابكم 1234567 - 001 بقيمة 9.228 دينار اردني من MERCHANT DUBLIN"
        assertTrue(SmsParser.isBankSms("JIB", body))
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
