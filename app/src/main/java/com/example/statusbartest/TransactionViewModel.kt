package com.example.statusbartest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusbartest.AppDatabase
import com.example.statusbartest.Transaction
import com.example.statusbartest.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: Flow<List<Transaction>>
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions
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
}