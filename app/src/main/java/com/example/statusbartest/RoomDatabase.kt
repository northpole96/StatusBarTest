package com.example.statusbartest
//package com.example.statusbartest.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Transaction Entity - Represents a financial transaction in the database
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "Expense" or "Income"
    val date: String, // Stored as String, will be converted using TypeConverters
    val time: String = "00:00", // Time as HH:mm format
    val category: String = "Other", // Transaction category
    val notes: String = "", // Optional notes about the transaction
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Type Converters for LocalDate <-> String and LocalTime <-> String conversion
 */
class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, dateFormatter)
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString, timeFormatter)
    }
}

/**
 * Data Access Object (DAO) - Interface defining database operations
 */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

/**
 * Repository - Abstracts data operations for the ViewModel
 */
class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
}


/**
 * Category Entity - Represents a transaction category in the database
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val type: String, // "Expense" or "Income"
    val isDefault: Boolean = false,
    val isSuggested: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data Access Object (DAO) - Interface defining database operations for categories
 */
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND isDefault = 0 AND isSuggested = 0 ORDER BY name ASC")
    fun getCustomCategories(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND isSuggested = 1 ORDER BY name ASC")
    fun getSuggestedCategories(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND (isDefault = 1 OR isSuggested = 0) ORDER BY name ASC")
    fun getUserCategories(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteById(id: Long): Int
}

/**
 * Repository - Abstracts data operations for the categories
 */
class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getDefaultCategories(type: String): Flow<List<Category>> =
        categoryDao.getDefaultCategories(type)

    fun getCustomCategories(type: String): Flow<List<Category>> =
        categoryDao.getCustomCategories(type)

    fun getSuggestedCategories(type: String): Flow<List<Category>> =
        categoryDao.getSuggestedCategories(type)

    fun getUserCategories(type: String): Flow<List<Category>> =
        categoryDao.getUserCategories(type)

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)

    suspend fun insert(category: Category): Long =
        categoryDao.insert(category)

    suspend fun update(category: Category) =
        categoryDao.update(category)

    suspend fun delete(category: Category) =
        categoryDao.delete(category)

    suspend fun deleteById(id: Long): Int =
        categoryDao.deleteById(id)
}



/**
 * Database - Main access point for the app's persisted data
 */
/**
 * Database - Main access point for the app's persisted data
 * With migration from v1 to v2 to add time, category, and notes fields
 */
@Database(
    entities = [Transaction::class, Category::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transaction_database"
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from schema version 1 to 2 (existing migration)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns: time, category, and notes
                database.execSQL("ALTER TABLE transactions ADD COLUMN time TEXT NOT NULL DEFAULT '00:00'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        // Migration from schema version 2 to 3 (adding categories table)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create categories table
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        type TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        isSuggested INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate with default categories when the database is created
                // This will be done in the CategoryViewModel
            }
        }
    }
}

