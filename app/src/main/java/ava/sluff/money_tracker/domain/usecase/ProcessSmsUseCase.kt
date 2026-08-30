package ava.sluff.money_tracker.domain.usecase

import android.util.Log
import ava.sluff.money_tracker.ai.TransactionCategorizer
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.local.entity.TransactionEntity
import ava.sluff.money_tracker.data.repository.CategoryRepository
import ava.sluff.money_tracker.data.repository.TransactionRepository
import ava.sluff.money_tracker.notification.CategoryNotificationManager
import ava.sluff.money_tracker.sms.SmsAmountExtractor
import ava.sluff.money_tracker.sms.SmsParser
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProcessSmsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionCategorizer: TransactionCategorizer,
    private val notificationManager: CategoryNotificationManager,
    private val settingsDataStore: SettingsDataStore
) {

    suspend operator fun invoke(sender: String, smsBody: String) {
        try {
            if (!SmsParser.isBankSms(sender, smsBody)) {
                Log.d(TAG, "Not a bank SMS, ignoring")
                return
            }
            if (transactionRepository.isDuplicate(smsBody)) {
                Log.d(TAG, "Duplicate SMS, ignoring")
                return
            }

            val baseUrl = settingsDataStore.baseUrl.first()
            val apiKey = settingsDataStore.apiKey.first()
            val model = settingsDataStore.modelName.first()
            val currency = settingsDataStore.currency.first()

            val result = transactionCategorizer.categorize(smsBody, baseUrl, apiKey, model, currency)
            if (result == null) {
                Log.e(TAG, "Categorization failed, storing uncategorized")
            }

            val amount = result?.amount ?: SmsAmountExtractor.amount(smsBody) ?: 0.0
            if (amount <= 0.0) {
                // Neither the model nor the local regex found an amount, so this is not a
                // transaction — usually a marketing message that happened to mention a number.
                // Storing it would only pollute totals with a row carrying no information.
                Log.d(TAG, "No usable amount, ignoring")
                return
            }

            val matchedCategory = result?.let { categoryRepository.getCategoryByName(it.categoryName) }
            val confident = result != null && result.confidence >= CONFIDENCE_THRESHOLD && matchedCategory != null

            val entity = TransactionEntity(
                amount = amount,
                type = (result?.type ?: ava.sluff.money_tracker.domain.model.TransactionType.DEBIT).name,
                merchantName = result?.merchantName,
                description = result?.description,
                categoryId = if (confident) matchedCategory?.id else null,
                rawSms = smsBody,
                smsSender = sender,
                timestamp = System.currentTimeMillis(),
                balanceAfter = result?.balanceAfter ?: SmsAmountExtractor.balanceAfter(smsBody),
                isCategorizedByAi = confident,
                aiConfidence = result?.confidence,
                note = null
            )
            val transactionId = transactionRepository.insert(entity)
            Log.d(TAG, "Inserted transaction $transactionId confident=$confident")

            if (!confident) {
                val all = categoryRepository.getAllCategories().first()
                val suggested = matchedCategory?.let { listOf(it.id to it.name) } ?: emptyList()
                val rest = all
                    .filter { c -> suggested.none { it.first == c.id } }
                    .map { it.id to it.name }
                notificationManager.showCategoryPrompt(
                    transactionId = transactionId,
                    amount = entity.amount,
                    merchantName = entity.merchantName,
                    topCategories = (suggested + rest).take(3)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process SMS", e)
        }
    }

    companion object {
        private const val TAG = "MoneyTracker.Process"
        const val CONFIDENCE_THRESHOLD = 0.7f
    }
}
