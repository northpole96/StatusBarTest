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
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.time.YearMonth

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: Flow<List<Transaction>> = _allTransactions

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
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
                LocalDate.parse(it.date, formatter) == date
            }
            FilterType.WEEK -> transactions.filter {
                val transactionDate = LocalDate.parse(it.date, formatter)
                val transactionWeekStart = transactionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                transactionWeekStart == weekStart
            }
            FilterType.MONTH -> transactions.filter {
                YearMonth.from(LocalDate.parse(it.date, formatter)) == month
            }
            FilterType.CATEGORY -> transactions
            FilterType.ALL -> transactions
        }

        // Apply category filter
        filteredTransactions = when(category) {
            "Income" -> filteredTransactions.filter { it.type == "Income" }
            "Expense" -> filteredTransactions.filter { it.type == "Expense" }
            else -> filteredTransactions
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

    fun insert(amount: Double, type: String, date: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            amount = amount,
            type = type,
            date = date.format(formatter)
        )
        repository.insert(transaction)
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