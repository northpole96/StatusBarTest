package com.example.statusbartest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusbartest.AppDatabase
import com.example.statusbartest.Transaction
import com.example.statusbartest.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: Flow<List<Transaction>>
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val filterDate = MutableStateFlow<LocalDate?>(null)
    private val filterType = MutableStateFlow<FilterType>(FilterType.ALL)

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = combine(filterDate, filterType, repository.allTransactions) { date, type, transactions ->
            filterTransactions(transactions, date, type)
        }
    }

    private fun filterTransactions(
        transactions: List<Transaction>,
        date: LocalDate?,
        type: FilterType
    ): List<Transaction> {
        return transactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date, formatter)
            val dateMatches = date?.let {
                when (type) {
                    FilterType.DAY -> transactionDate == it
                    FilterType.WEEK -> transactionDate in it.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))..it.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                    FilterType.MONTH -> transactionDate.year == it.year && transactionDate.month == it.month
                    FilterType.ALL -> true
                }
            } ?: true
            dateMatches
        }
    }

    fun setFilterDate(date: LocalDate?) {
        filterDate.value = date
    }

    fun setFilterType(type: FilterType) {
        filterType.value = type
    }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun insert(amount: Double, type: String, date: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        val transaction = Transaction(
            amount = amount,
            type = type,
            date = date.format(formatter)
        )
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    enum class FilterType {
        ALL, DAY, WEEK, MONTH
    }
}
