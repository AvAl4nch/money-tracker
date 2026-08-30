package ava.sluff.money_tracker.data.repository

import android.database.Cursor
import ava.sluff.money_tracker.ai.SqlGuard
import ava.sluff.money_tracker.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val database: AppDatabase
) {

    /**
     * Runs an already-guarded statement and materialises its rows.
     *
     * The work happens inside a transaction that is deliberately never marked successful, so
     * even a statement that slipped past [SqlGuard] cannot commit anything. Rows are read
     * before the transaction ends because the cursor is invalid afterwards.
     */
    suspend fun runReadOnlyQuery(sql: String): QueryResult = withContext(Dispatchers.IO) {
        val db = database.openHelper.writableDatabase
        db.beginTransaction()
        try {
            db.query(sql).use { cursor -> read(cursor) }
        } finally {
            // No setTransactionSuccessful(): the transaction always rolls back.
            db.endTransaction()
        }
    }

    private fun read(cursor: Cursor): QueryResult {
        val columns = cursor.columnNames.toList()
        val rows = mutableListOf<List<String>>()
        var truncated = false
        while (cursor.moveToNext()) {
            if (rows.size >= SqlGuard.ROW_LIMIT) {
                truncated = true
                break
            }
            rows += (0 until cursor.columnCount).map { i ->
                if (cursor.isNull(i)) "" else cursor.getString(i)
            }
        }
        return QueryResult(columns, rows, truncated)
    }
}
