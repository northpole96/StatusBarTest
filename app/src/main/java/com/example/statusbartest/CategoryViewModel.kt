package com.example.statusbartest

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepository

    // Category data flows
    val expenseDefaultCategories: Flow<List<Category>>
    val expenseCustomCategories: Flow<List<Category>>
    val expenseSuggestedCategories: Flow<List<Category>>

    val incomeDefaultCategories: Flow<List<Category>>
    val incomeCustomCategories: Flow<List<Category>>
    val incomeSuggestedCategories: Flow<List<Category>>

    // For the category add/edit sheet
    private val _selectedCategoryType = MutableStateFlow("Expense")
    val selectedCategoryType: StateFlow<String> = _selectedCategoryType.asStateFlow()

    // Predefined colors for categories
    val predefinedColors = listOf(
        "#F44336", // Red
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#673AB7", // Deep Purple
        "#3F51B5", // Indigo
        "#2196F3", // Blue
        "#03A9F4", // Light Blue
        "#00BCD4", // Cyan
        "#009688", // Teal
        "#4CAF50", // Green
        "#8BC34A", // Light Green
        "#CDDC39", // Lime
        "#FFEB3B", // Yellow
        "#FFC107", // Amber
        "#FF9800", // Orange
        "#FF5722"  // Deep Orange
    )

    // Default emoji options (can be expanded)
    val defaultEmojis = listOf(
        "🍔", "🍕", "🛒", "🏠", "💊", "🚗", "✈️", "🎬", "🎮", "📚",
        "👕", "💼", "💰", "💵", "💳", "🏦", "📱", "💻", "🎁", "🎓",
        "🏥", "🚉", "☕", "🍷", "🛍️", "🧾", "📊", "🎯", "🔋", "🔌",
        "🧳", "🧴", "🧷", "🧹", "🧺", "📦", "📝", "📌", "🎨", "🎭"
    )

    // Default categories that come with the app
    private val defaultExpenseCategories = listOf(
        Category(name = "Food", emoji = "🍔", colorHex = "#F44336", type = "Expense", isDefault = true),
        Category(name = "Transport", emoji = "🚗", colorHex = "#2196F3", type = "Expense", isDefault = true),
        Category(name = "Shopping", emoji = "🛒", colorHex = "#E91E63", type = "Expense", isDefault = true),
        Category(name = "Bills", emoji = "🧾", colorHex = "#4CAF50", type = "Expense", isDefault = true),
        Category(name = "Health", emoji = "💊", colorHex = "#00BCD4", type = "Expense", isDefault = true),
        Category(name = "Other", emoji = "📦", colorHex = "#9E9E9E", type = "Expense", isDefault = true)
    )

    private val defaultIncomeCategories = listOf(
        Category(name = "Salary", emoji = "💰", colorHex = "#4CAF50", type = "Income", isDefault = true),
        Category(name = "Freelance", emoji = "💼", colorHex = "#2196F3", type = "Income", isDefault = true),
        Category(name = "Gift", emoji = "🎁", colorHex = "#FF9800", type = "Income", isDefault = true),
        Category(name = "Investment", emoji = "📊", colorHex = "#673AB7", type = "Income", isDefault = true),
        Category(name = "Other", emoji = "📦", colorHex = "#9E9E9E", type = "Income", isDefault = true)
    )

    // Suggested categories the user can quickly add
    private val suggestedExpenseCategories = listOf(
        Category(name = "Entertainment", emoji = "🎬", colorHex = "#9C27B0", type = "Expense", isSuggested = true),
        Category(name = "Gifts", emoji = "🎁", colorHex = "#FF5722", type = "Expense", isSuggested = true),
        Category(name = "Education", emoji = "📚", colorHex = "#3F51B5", type = "Expense", isSuggested = true),
        Category(name = "Home", emoji = "🏠", colorHex = "#009688", type = "Expense", isSuggested = true),
        Category(name = "Travel", emoji = "✈️", colorHex = "#FFC107", type = "Expense", isSuggested = true),
        Category(name = "Coffee", emoji = "☕", colorHex = "#795548", type = "Expense", isSuggested = true)
    )

    private val suggestedIncomeCategories = listOf(
        Category(name = "Bonus", emoji = "💵", colorHex = "#8BC34A", type = "Income", isSuggested = true),
        Category(name = "Refund", emoji = "💳", colorHex = "#03A9F4", type = "Income", isSuggested = true),
        Category(name = "Selling", emoji = "🛍️", colorHex = "#FF5722", type = "Income", isSuggested = true),
        Category(name = "Interest", emoji = "🏦", colorHex = "#673AB7", type = "Income", isSuggested = true),
        Category(name = "Allowance", emoji = "💰", colorHex = "#4CAF50", type = "Income", isSuggested = true)
    )

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)

        // Initialize the flow properties
        expenseDefaultCategories = repository.getDefaultCategories("Expense")
        expenseCustomCategories = repository.getCustomCategories("Expense")
        expenseSuggestedCategories = repository.getSuggestedCategories("Expense")

        incomeDefaultCategories = repository.getDefaultCategories("Income")
        incomeCustomCategories = repository.getCustomCategories("Income")
        incomeSuggestedCategories = repository.getSuggestedCategories("Income")

        // Initialize the database with default and suggested categories
        initializeCategories()
    }

    private fun initializeCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if default categories exist
            val existingExpenseDefaults = repository.getDefaultCategories("Expense")
                .combine(repository.getSuggestedCategories("Expense")) { defaults, suggested ->
                    defaults.size to suggested.size
                }

            val existingIncomeDefaults = repository.getDefaultCategories("Income")
                .combine(repository.getSuggestedCategories("Income")) { defaults, suggested ->
                    defaults.size to suggested.size
                }

            // Only add default categories if they don't already exist
            existingExpenseDefaults.collect { (defaultCount, suggestedCount) ->
                if (defaultCount == 0) {
                    defaultExpenseCategories.forEach { category ->
                        repository.insert(category)
                    }
                }

                if (suggestedCount == 0) {
                    suggestedExpenseCategories.forEach { category ->
                        repository.insert(category)
                    }
                }

                return@collect // Exit the collect scope after first emission
            }

            existingIncomeDefaults.collect { (defaultCount, suggestedCount) ->
                if (defaultCount == 0) {
                    defaultIncomeCategories.forEach { category ->
                        repository.insert(category)
                    }
                }

                if (suggestedCount == 0) {
                    suggestedIncomeCategories.forEach { category ->
                        repository.insert(category)
                    }
                }

                return@collect // Exit the collect scope after first emission
            }
        }
    }

    fun setSelectedCategoryType(type: String) {
        _selectedCategoryType.value = type
    }

    fun addCategory(name: String, emoji: String, colorHex: String, type: String) = viewModelScope.launch(Dispatchers.IO) {
        val category = Category(
            name = name,
            emoji = emoji,
            colorHex = colorHex,
            type = type,
            isDefault = false,
            isSuggested = false
        )
        repository.insert(category)
    }

    fun updateCategory(id: Long, name: String, emoji: String, colorHex: String, type: String) = viewModelScope.launch(Dispatchers.IO) {
        val existingCategory = repository.getCategoryById(id) ?: return@launch

        // Don't allow changing isDefault or isSuggested status
        val updatedCategory = existingCategory.copy(
            name = name,
            emoji = emoji,
            colorHex = colorHex,
            type = type
        )

        repository.update(updatedCategory)
    }

    fun deleteCategory(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    fun addSuggestedCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        // Add a suggested category but mark it as no longer suggested
        val newCategory = category.copy(
            id = 0, // Auto-generate new ID
            isSuggested = false,
            isDefault = false,
            createdAt = System.currentTimeMillis()
        )
        repository.insert(newCategory)
    }

    fun getCategoryColor(colorHex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    // Function to get all available categories for a specific transaction type
    fun getCategoriesForType(type: String): Flow<List<Category>> {
        return if (type == "Expense") {
            repository.getUserCategories("Expense")
        } else {
            repository.getUserCategories("Income")
        }
    }
}