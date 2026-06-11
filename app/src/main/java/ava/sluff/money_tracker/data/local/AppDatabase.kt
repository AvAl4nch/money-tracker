package ava.sluff.money_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ava.sluff.money_tracker.data.local.dao.BudgetDao
import ava.sluff.money_tracker.data.local.dao.CategoryDao
import ava.sluff.money_tracker.data.local.dao.TransactionDao
import ava.sluff.money_tracker.data.local.entity.BudgetEntity
import ava.sluff.money_tracker.data.local.entity.CategoryEntity
import ava.sluff.money_tracker.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DB_NAME = "money_tracker.db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `budgets` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`category_id` INTEGER NOT NULL, " +
                        "`monthly_limit` REAL NOT NULL, " +
                        "FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_category_id` " +
                        "ON `budgets` (`category_id`)"
                )
            }
        }

        // Exact values from the original database.
        val SEED_CATEGORIES = listOf(
            CategoryEntity(name = "Groceries", icon = "shopping_cart", color = 4283215696L, isDefault = true, sortOrder = 0),
            CategoryEntity(name = "Transport", icon = "directions_car", color = 4280391411L, isDefault = true, sortOrder = 1),
            CategoryEntity(name = "Dining", icon = "restaurant", color = 4294940672L, isDefault = true, sortOrder = 2),
            CategoryEntity(name = "Entertainment", icon = "movie", color = 4288423856L, isDefault = true, sortOrder = 3),
            CategoryEntity(name = "Shopping", icon = "shopping_bag", color = 4293467747L, isDefault = true, sortOrder = 4),
            CategoryEntity(name = "Health", icon = "local_hospital", color = 4294198070L, isDefault = true, sortOrder = 5),
            CategoryEntity(name = "Bills & Utilities", icon = "receipt_long", color = 4284513675L, isDefault = true, sortOrder = 6),
            CategoryEntity(name = "Education", icon = "school", color = 4282339765L, isDefault = true, sortOrder = 7),
            CategoryEntity(name = "Transfers", icon = "swap_horiz", color = 4278238420L, isDefault = true, sortOrder = 8),
            CategoryEntity(name = "Salary", icon = "account_balance_wallet", color = 4287349578L, isDefault = true, sortOrder = 9),
            CategoryEntity(name = "ATM Withdrawal", icon = "atm", color = 4294924066L, isDefault = true, sortOrder = 10),
            CategoryEntity(name = "Other", icon = "more_horiz", color = 4288585374L, isDefault = true, sortOrder = 11)
        )
    }
}
