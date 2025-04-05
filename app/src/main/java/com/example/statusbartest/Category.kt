package com.example.statusbartest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryManagerScreen(
    navController: NavController,
    viewModel: CategoryViewModel
) {
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    val selectedCategoryType by viewModel.selectedCategoryType.collectAsState()

    // Get categories by type
    val defaultCategories by if (selectedCategoryType == "Expense")
        viewModel.expenseDefaultCategories.collectAsState(initial = emptyList())
    else
        viewModel.incomeDefaultCategories.collectAsState(initial = emptyList())

    val customCategories by if (selectedCategoryType == "Expense")
        viewModel.expenseCustomCategories.collectAsState(initial = emptyList())
    else
        viewModel.incomeCustomCategories.collectAsState(initial = emptyList())

    val suggestedCategories by if (selectedCategoryType == "Expense")
        viewModel.expenseSuggestedCategories.collectAsState(initial = emptyList())
    else
        viewModel.incomeSuggestedCategories.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Manager") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    categoryToEdit = null
                    showAddCategorySheet = true
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Type Selector
            TransactionTypeSelectionBarWithState(
                isExpense = selectedCategoryType == "Expense",
                onTransactionTypeChange = { isExpense ->
                    viewModel.setSelectedCategoryType(if (isExpense) "Expense" else "Income")
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Default Categories Section
                item {
                    Text(
                        text = "Default Categories",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (defaultCategories.isEmpty()) {
                    item {
                        Text(
                            text = "No default categories",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(defaultCategories) { category ->
                        CategoryItem(
                            category = category,
                            onEditClick = { /* Default categories can't be edited */ },
                            onDeleteClick = { /* Default categories can't be deleted */ },
                            isDefault = true
                        )
                    }
                }

                // Custom Categories Section
                item {
                    Text(
                        text = "Your Categories",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                if (customCategories.isEmpty()) {
                    item {
                        Text(
                            text = "You haven't added any custom categories yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(customCategories) { category ->
                        CategoryItem(
                            category = category,
                            onEditClick = {
                                categoryToEdit = category
                                showAddCategorySheet = true
                            },
                            onDeleteClick = {
                                viewModel.deleteCategory(category.id)
                            }
                        )
                    }
                }

                // Suggested Categories Section
                if (suggestedCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Suggested Categories",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        SuggestedCategoriesGrid(
                            categories = suggestedCategories,
                            onCategoryClick = { category ->
                                viewModel.addSuggestedCategory(category)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Category Sheet
    if (showAddCategorySheet) {
        CategoryBottomSheet(
            onDismiss = { showAddCategorySheet = false },
            onSave = { name, emoji, colorHex, type ->
                if (categoryToEdit != null) {
                    viewModel.updateCategory(
                        id = categoryToEdit!!.id,
                        name = name,
                        emoji = emoji,
                        colorHex = colorHex,
                        type = type
                    )
                } else {
                    viewModel.addCategory(
                        name = name,
                        emoji = emoji,
                        colorHex = colorHex,
                        type = type
                    )
                }
                showAddCategorySheet = false
            },
            category = categoryToEdit,
            viewModel = viewModel
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDefault: Boolean = false
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji and color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category name
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Action buttons (only for non-default categories)
            if (!isDefault) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }

                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete the '${category.name}' category?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestedCategoriesGrid(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            SuggestedCategoryChip(category = category, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun SuggestedCategoryChip(
    category: Category,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${category.emoji} ${category.name}",
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = categoryColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorHex: String, type: String) -> Unit,
    category: Category? = null,
    viewModel: CategoryViewModel
) {
    val isEditMode = category != null

    var categoryName by remember { mutableStateOf(category?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(category?.emoji ?: "📦") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: viewModel.predefinedColors.first()) }
    var selectedType by remember { mutableStateOf(category?.type ?: viewModel.selectedCategoryType.value) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var showEmojiPicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        if (!isEditMode) {
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Title
            Text(
                text = if (isEditMode) "Edit Category" else "Add Category",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Type selector (Expense/Income)
            TransactionTypeSelectionBarWithState(
                isExpense = selectedType == "Expense",
                onTransactionTypeChange = { isExpense ->
                    selectedType = if (isExpense) "Expense" else "Income"
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emoji selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Icon:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(80.dp)
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = try {
                                Color(android.graphics.Color.parseColor(selectedColor))
                            } catch (e: Exception) {
                                Color.Gray
                            },
                            shape = CircleShape
                        )
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedEmoji,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { showEmojiPicker = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.5f),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Choose Icon")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category name input
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color selector
            Text(
                text = "Select Color:",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorSelector(
                colors = viewModel.predefinedColors,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onSave(categoryName, selectedEmoji, selectedColor, selectedType)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = categoryName.isNotBlank()
            ) {
                Text(text = if (isEditMode) "Update Category" else "Add Category")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        EmojiPickerDialog(
            emojis = viewModel.defaultEmojis,
            onEmojiSelected = { emoji ->
                selectedEmoji = emoji
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

// Utility extension function to get the display color for a category from its hex string
fun Category.displayColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSelector(
    colors: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { colorHex ->
            ColorItem(
                colorHex = colorHex,
                isSelected = colorHex == selectedColor,
                onClick = { onColorSelected(colorHex) }
            )
        }
    }
}

@Composable
fun ColorItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.Black,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmojiPickerDialog(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val rows = emojis.chunked(6)
                items(rows) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { onEmojiSelected(emoji) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}