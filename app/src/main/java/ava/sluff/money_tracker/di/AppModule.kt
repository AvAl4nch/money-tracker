package ava.sluff.money_tracker.di

import android.content.ContentValues
import android.content.Context
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ava.sluff.money_tracker.data.local.AppDatabase
import ava.sluff.money_tracker.data.local.dao.BudgetDao
import ava.sluff.money_tracker.data.local.dao.CategoryDao
import ava.sluff.money_tracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    AppDatabase.SEED_CATEGORIES.forEach { c ->
                        val values = ContentValues().apply {
                            put("name", c.name)
                            put("icon", c.icon)
                            put("color", c.color)
                            put("is_default", if (c.isDefault) 1 else 0)
                            put("sort_order", c.sortOrder)
                        }
                        db.insert("categories", OnConflictStrategy.IGNORE, values)
                    }
                }
            })
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
}
