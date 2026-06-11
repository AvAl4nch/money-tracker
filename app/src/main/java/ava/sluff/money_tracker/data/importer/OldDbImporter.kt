package ava.sluff.money_tracker.data.importer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.withTransaction
import ava.sluff.money_tracker.data.local.AppDatabase
import ava.sluff.money_tracker.data.local.dao.CategoryDao
import ava.sluff.money_tracker.data.local.dao.TransactionDao
import ava.sluff.money_tracker.data.local.entity.CategoryEntity
import ava.sluff.money_tracker.data.local.entity.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OldDbImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    data class ImportResult(val imported: Int, val skipped: Int)

    private data class OldCategory(
        val id: Long,
        val name: String,
        val icon: String,
        val color: Long,
        val isDefault: Boolean,
        val sortOrder: Int
    )

    private data class OldTransaction(
        val amount: Double,
        val type: String,
        val merchantName: String?,
        val description: String?,
        val categoryId: Long?,
        val rawSms: String,
        val smsSender: String,
        val timestamp: Long,
        val balanceAfter: Double?,
        val isCategorizedByAi: Boolean,
        val aiConfidence: Float?,
        val note: String?
    )

    /**
     * Copies only the main DB file. If the source was exported from a live app with an
     * uncheckpointed -wal sidecar, the newest rows may be absent — export after the app
     * is closed, or checkpoint first. The Phase 1 source file was verified checkpointed.
     */
    suspend fun import(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "import_old.db")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext Result.failure(IllegalArgumentException("Cannot open selected file"))

            // Read everything from the OLD SQLite file into memory, then close it.
            val oldCategories = mutableListOf<OldCategory>()
            val oldTransactions = mutableListOf<OldTransaction>()
            SQLiteDatabase.openDatabase(
                tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
            ).use { db ->
                db.rawQuery("SELECT id, name, icon, color, is_default, sort_order FROM categories", null)
                    .use { cursor ->
                        while (cursor.moveToNext()) {
                            oldCategories += OldCategory(
                                id = cursor.getLong(0),
                                name = cursor.getString(1),
                                icon = cursor.getString(2),
                                color = cursor.getLong(3),
                                isDefault = cursor.getInt(4) == 1,
                                sortOrder = cursor.getInt(5)
                            )
                        }
                    }
                db.rawQuery(
                    """SELECT amount, type, merchant_name, description, category_id, raw_sms,
                              sms_sender, timestamp, balance_after, is_categorized_by_ai, ai_confidence, note
                       FROM transactions""",
                    null
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        oldTransactions += OldTransaction(
                            amount = cursor.getDouble(0),
                            type = cursor.getString(1),
                            merchantName = if (cursor.isNull(2)) null else cursor.getString(2),
                            description = if (cursor.isNull(3)) null else cursor.getString(3),
                            categoryId = if (cursor.isNull(4)) null else cursor.getLong(4),
                            rawSms = cursor.getString(5),
                            smsSender = cursor.getString(6),
                            timestamp = cursor.getLong(7),
                            balanceAfter = if (cursor.isNull(8)) null else cursor.getDouble(8),
                            isCategorizedByAi = cursor.getInt(9) == 1,
                            aiConfidence = if (cursor.isNull(10)) null else cursor.getFloat(10),
                            note = if (cursor.isNull(11)) null else cursor.getString(11)
                        )
                    }
                }
            }

            // All writes happen atomically: remap categories + dedupe + insert transactions.
            // Any exception inside withTransaction rolls back everything (no partial writes).
            val result = appDatabase.withTransaction {
                // 1. Map old category id -> new category id (insert custom categories when missing)
                val categoryIdMap = mutableMapOf<Long, Long>()
                for (oldCategory in oldCategories) {
                    val existing = categoryDao.getCategoryByName(oldCategory.name)
                    val newId = existing?.id ?: categoryDao.insert(
                        CategoryEntity(
                            name = oldCategory.name,
                            icon = oldCategory.icon,
                            color = oldCategory.color,
                            isDefault = oldCategory.isDefault,
                            sortOrder = oldCategory.sortOrder
                        )
                    )
                    categoryIdMap[oldCategory.id] = newId
                }

                // 2. Import transactions, skipping raw-SMS duplicates
                var imported = 0
                var skipped = 0
                for (old in oldTransactions) {
                    val rawSms = old.rawSms
                    if (rawSms.isNotBlank() && transactionDao.countByRawSms(rawSms) > 0) {
                        skipped++
                        continue
                    }
                    transactionDao.insert(
                        TransactionEntity(
                            amount = old.amount,
                            type = old.type,
                            merchantName = old.merchantName,
                            description = old.description,
                            categoryId = old.categoryId?.let { categoryIdMap[it] },
                            rawSms = rawSms,
                            smsSender = old.smsSender,
                            timestamp = old.timestamp,
                            balanceAfter = old.balanceAfter,
                            isCategorizedByAi = old.isCategorizedByAi,
                            aiConfidence = old.aiConfidence,
                            note = old.note
                        )
                    )
                    imported++
                }
                ImportResult(imported, skipped)
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempFile.delete()
        }
    }
}
