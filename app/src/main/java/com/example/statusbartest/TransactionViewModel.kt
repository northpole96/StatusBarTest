package com.example.statusbartest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.time.YearMonth

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: Flow<List<Transaction>> = _allTransactions

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _filterType = MutableStateFlow<FilterType>(FilterType.ALL)
    val filterType: Flow<FilterType> = _filterType

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: Flow<LocalDate> = _currentDate

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: Flow<YearMonth> = _currentMonth

    private val _currentWeekStart = MutableStateFlow(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    val currentWeekStart: Flow<LocalDate> = _currentWeekStart

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: Flow<String> = _selectedCategory

    // Available expense categories
    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other")

    // Available income categories
    val incomeCategories = listOf("Salary", "Freelance", "Gift", "Investment", "Other")

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        viewModelScope.launch {
            repository.allTransactions.collect { transactions ->
                _allTransactions.value = transactions
            }
        }
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }
    fun deleteAllTransactions() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun generateTestTransactions(count: Int = 500) = viewModelScope.launch(Dispatchers.IO) {
        val random = java.util.Random()
        val today = LocalDate.now()
        
        // Generate transactions for the last 60 days
        for (i in 0 until count) {
            // Random date within the last 60 days
            val daysAgo = random.nextInt(60)
            val date = today.minusDays(daysAgo.toLong())
            
            // Random time
            val hour = random.nextInt(24)
            val minute = random.nextInt(60)
            val time = LocalTime.of(hour, minute)
            
            // Random type (70% expense, 30% income)
            val type = if (random.nextFloat() < 0.7f) "Expense" else "Income"
            
            // Select category based on type
            val categories = if (type == "Expense") {
                expenseCategories
            } else {
                incomeCategories
            }
            val category = categories[random.nextInt(categories.size)]
            
            // Random amount between $1 and $1000
            val amount = 1 + random.nextDouble() * 999
            
            // Generate notes
            val notes = when {
                category == "Food" -> listOf(
                    "Groceries", "Restaurant", "Coffee", "Lunch", "Dinner", "Breakfast"
                )
                category == "Transport" -> listOf(
                    "Gas", "Uber", "Train ticket", "Bus fare", "Taxi"
                )
                category == "Shopping" -> listOf(
                    "Clothes", "Electronics", "Household items", "Books", "Gifts"
                )
                category == "Salary" -> listOf(
                    "Monthly salary", "Bonus", "Overtime"
                )
                else -> listOf(
                    "Regular payment", "One-time", "Special", "Planned", "Unexpected"
                )
            }
            val note = notes[random.nextInt(notes.size)]
            
            // Insert the transaction
            insert(
                amount = amount,
                type = type,
                date = date,
                time = time,
                category = category,
                notes = note
            )
        }
    }

    fun filterTransactions(
        transactions: List<Transaction>,
        type: FilterType,
        date: LocalDate,
        month: YearMonth,
        weekStart: LocalDate,
        category: String = "All"
    ): List<Transaction> {
        var filteredTransactions = when(type) {
            FilterType.DAY -> transactions.filter {
                LocalDate.parse(it.date, dateFormatter) == date
            }
            FilterType.WEEK -> transactions.filter {
                val transactionDate = LocalDate.parse(it.date, dateFormatter)
                val transactionWeekStart = transactionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                transactionWeekStart == weekStart
            }
            FilterType.MONTH -> transactions.filter {
                YearMonth.from(LocalDate.parse(it.date, dateFormatter)) == month
            }
            FilterType.CATEGORY -> transactions
            FilterType.ALL -> transactions
        }

        // Apply category filter if specified
        filteredTransactions = when(category) {
            "Income" -> filteredTransactions.filter { it.type == "Income" }
            "Expense" -> filteredTransactions.filter { it.type == "Expense" }
            "All" -> filteredTransactions
            else -> filteredTransactions.filter { it.category == category } // Filter by specific category
        }

        return filteredTransactions
    }

    fun calculateTotalSpent(
        transactions: List<Transaction>,
        period: String
    ): Double {
        val now = LocalDate.now()
        return transactions
            .filter { it.type == "Expense" }
            .filter { transaction ->
                val transactionDate = LocalDate.parse(transaction.date)
                when (period) {
                    "Today" -> transactionDate == now
                    "This Week" -> transactionDate.isAfter(now.minusWeeks(1))
                    "This Month" -> transactionDate.month == now.month
                    "This Year" -> transactionDate.year == now.year
                    else -> true // All Time
                }
            }
            .sumOf { it.amount }
    }

    fun insert(
        amount: Double,
        type: String,
        date: LocalDate,
        time: LocalTime = LocalTime.now(),
        category: String = "Other",
        notes: String = ""
    ) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            amount = amount,
            type = type,
            date = date.format(dateFormatter),
            time = time.format(timeFormatter),
            category = category,
            notes = notes
        )
        repository.insert(transaction)
    }


    fun updateTransaction(
        id: Long,
        amount: Double,
        type: String,
        date: LocalDate,
        time: LocalTime = LocalTime.now(),
        category: String = "Other",
        notes: String = ""
    ) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            id = id,
            amount = amount,
            type = type,
            date = date.format(dateFormatter),
            time = time.format(timeFormatter),
            category = category,
            notes = notes,
            createdAt = System.currentTimeMillis() // You might want to keep the original creation time instead
        )
        repository.update(transaction)
    }


    fun deleteTransaction(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val transactionToDelete = _allTransactions.value.find { it.id == id } ?: return@launch
        repository.delete(transactionToDelete)
    }

    fun nextDay() {
        _currentDate.value = _currentDate.value.plusDays(1)
    }

    fun previousDay() {
        _currentDate.value = _currentDate.value.minusDays(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
    }

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
    }

    enum class FilterType {
        ALL, DAY, WEEK, MONTH, CATEGORY
    }
}