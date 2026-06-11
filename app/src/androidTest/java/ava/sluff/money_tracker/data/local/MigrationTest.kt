package ava.sluff.money_tracker.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate2To3_preservesDataAndAddsBudgets() {
        helper.createDatabase("migration-test.db", 2).use { db ->
            db.execSQL(
                "INSERT INTO categories (name, icon, color, is_default, sort_order) " +
                    "VALUES ('Groceries', 'shopping_cart', 4283215696, 1, 0)"
            )
            db.execSQL(
                "INSERT INTO transactions (amount, type, merchant_name, description, category_id, " +
                    "raw_sms, sms_sender, timestamp, balance_after, is_categorized_by_ai, ai_confidence, note) " +
                    "VALUES (9.99, 'DEBIT', 'M', 'd', 1, 'raw', 'JIB', 1748000000000, 100.0, 1, 0.9, NULL)"
            )
        }

        helper.runMigrationsAndValidate("migration-test.db", 3, true, AppDatabase.MIGRATION_2_3).use { db ->
            db.query("SELECT count(*) FROM transactions").use { c ->
                c.moveToFirst(); assertEquals(1, c.getInt(0))
            }
            db.query("SELECT count(*) FROM categories").use { c ->
                c.moveToFirst(); assertEquals(1, c.getInt(0))
            }
            db.execSQL("INSERT INTO budgets (category_id, monthly_limit) VALUES (1, 150.0)")
            db.query("SELECT monthly_limit FROM budgets WHERE category_id = 1").use { c ->
                c.moveToFirst(); assertEquals(150.0, c.getDouble(0), 0.0001)
            }
        }
    }
}
