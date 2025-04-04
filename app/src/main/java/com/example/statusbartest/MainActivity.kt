package com.example.statusbartest


import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import com.example.statusbartest.TransactionViewModel.FilterType
import com.example.statusbartest.ui.theme.StatusBarTestTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Search : BottomNavItem("search", Icons.Default.Search, "Search")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
    object Info : BottomNavItem("info", Icons.Default.Info, "Info")
}


@Composable

fun BottomNavBar(navController: NavController, items: List<BottomNavItem>,onItemClick: (BottomNavItem) -> Unit ) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
//hello first commit
    NavigationBar (containerColor=MaterialTheme.colorScheme.surface){

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.route == item.route,
                onClick = {
                    onItemClick(item)
                })
        }
    }
}
@Composable
fun CustomDigitKeyboard(
    input: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit // New callback parameter for save action
) {
    val colorButton = Color.LightGray.copy(alpha = 0.2f)
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "Save"),
//        listOf("Clear")
    )

    Column(
//        Modifier.border(0.dp, Color.Black, RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { label ->
                    if (label != "Save") {
                        Button(
                            onClick = {
                                when (label) {
                                    in "0".."9" -> {
                                        val updated = input + label
                                        if (isValidInput(updated)) {
                                            onInputChange(updated)
                                        }
                                    }

                                    "." -> {
                                        if (!input.contains(".")) {
                                            onInputChange(input + ".")
                                        }
                                    }

                                    "Del" -> {
                                        if (input.isNotEmpty()) {
                                            onInputChange(input.dropLast(1))
                                        }
                                    }

                                    "Clear" -> {
                                        onInputChange("")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorButton,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(horizontal = 0.dp, vertical = 0.dp)
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(
                                label,
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                color = Color.Black
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onSave() }, // Use the onSave callback here
                            modifier = Modifier
                                .padding(horizontal = 0.dp, vertical = 0.dp)
                                .weight(1f)
                                .height(60.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Favorite",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TransactionTypeSelectionBarWithState(
    isExpense: Boolean,
    onTransactionTypeChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), Arrangement.Center) {
        val interactionSource = remember { MutableInteractionSource() } // Prevent ripple

        Row(
            Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                .padding(4.dp)
        ) {
            Text(
                "Expense",
                Modifier
                    .background(
                        if (isExpense) Color.LightGray.copy(alpha = 0.4f) else Color.White,
                        RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable(interactionSource, indication = null) {
                        onTransactionTypeChange(true)
                    },
                fontWeight = FontWeight.Medium,
                color = if (isExpense) Color.Black else Color.Gray
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Income",
                Modifier
                    .background(
                        if (isExpense) Color.White else Color.LightGray.copy(alpha = 0.4f),
                        RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable(interactionSource, indication = null) {
                        onTransactionTypeChange(false)
                    },
                fontWeight = FontWeight.Medium,
                color = if (isExpense) Color.Gray else Color.Black
            )
        }
    }
}

fun isValidInput(input: String): Boolean {
    if (input.count { it == '.' } > 1) return false
    val parts = input.split(".")
    return if (parts.size == 2) {
        parts[1].length <= 2
    } else true
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel
) {
    var input by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Expense") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedCategory by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isNotesFieldFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // List of predefined categories
    val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other")
    val incomeCategories = listOf("Salary", "Freelance", "Gift", "Investment", "Other")

    // Get categories based on transaction type
    val categories = if (transactionType == "Expense") expenseCategories else incomeCategories

    // Function to save the transaction
    val saveTransaction = {
        if (selectedCategory.isNotEmpty()) {
            try {
                // Create a LocalDateTime from date and time
                val dateTime = LocalDateTime.of(selectedDate, selectedTime)

                // Save transaction to database
                // Properly handle empty input as 0.0
                val amount = if (input.isEmpty()) 0.0 else input.toDouble()

                viewModel.insert(
                    amount = amount,
                    type = transactionType,
                    date = selectedDate,
                    time = selectedTime,
                    category = selectedCategory,
                    notes = notes
                )
                onDismiss()
            } catch (e: NumberFormatException) {
                // Handle invalid input
                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp) )
            Box(modifier=Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                // Display "0" if input is empty, otherwise show the input
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (input.isEmpty()) "0" else input,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(8.dp)
                )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically){
                IconButton(onClick = {
                    if (input.isNotEmpty()) {
                        input = input.dropLast(1)
                        Log.d("input", input)
                    }
                }) {

                    Icon(
                        imageVector = Icons.Rounded.Backspace,
                        contentDescription = "Backspace",
                        Modifier.background(Color.LightGray.copy(0.4f), CircleShape).padding(12.dp),
                        tint = Color.Gray
                    )

                }
            }
            }
            Spacer(modifier = Modifier.height(16.dp))



            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Toggle
            TransactionTypeSelectionBarWithState(
                isExpense = transactionType == "Expense",
                onTransactionTypeChange = { isExpense ->
                    transactionType = if (isExpense) "Expense" else "Income"
                    // Reset category when type changes
                    selectedCategory = ""
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                var expandedCategory by remember { mutableStateOf(false) }

                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Select Category",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (expandedCategory) {
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(text = category) },
                                    onClick = {
                                        selectedCategory = category
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date and Time Picker
            var showDatePicker by remember { mutableStateOf(false) }
            var showTimePicker by remember { mutableStateOf(false) }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Column(Modifier.fillMaxWidth()) {
                    // Date Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    // Time Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Select Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Field with focus tracker
//            OutlinedTextField(
//                value = notes,
//                onValueChange = { notes = it },
//                label = { Text("Notes (Optional)") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .onFocusChanged { focusState ->
//                        isNotesFieldFocused = focusState.isFocused
//                    },
//                maxLines = 3,
//                shape = RoundedCornerShape(12.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    // Customize colors
//                    focusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
//                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
//                    focusedLabelColor = Color.Black,
//                    unfocusedLabelColor = Color.Black,
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.DarkGray,
//                    cursorColor = Color.Black,
//                )
//            )

//            Spacer(modifier = Modifier.height(16.dp))

            // Only show the CustomDigitKeyboard when notes field is not focused
            if (!isNotesFieldFocused) {
                CustomDigitKeyboard(
                    input = input,
                    onInputChange = { input = it },
                    onSave = saveTransaction
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                )

                DatePickerDialog(
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val newDate = Instant.ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                selectedDate = newDate
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(
                        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                        state = datePickerState,
                    )
                }
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedTime.hour,
                    initialMinute = selectedTime.minute
                )

                TimePickerDialog(
                    onDismissRequest = { showTimePicker = false },
                    onConfirm = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                    title = "Select Time"
                ) {
                    TimePicker(state = timePickerState,
                        colors=TimePickerDefaults.colors(
                           clockDialColor = Color.LightGray.copy(0.4f),
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorSelectedContainerColor = Color.Black,
                            timeSelectorUnselectedContentColor = Color.Black,
                            timeSelectorUnselectedContainerColor = Color.LightGray.copy(0.4f)

                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                content()

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onConfirm) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(window: Window, viewModel: TransactionViewModel) {
    val navController = rememberNavController()
    // Updated bottom nav items (removed Search)
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile,
        BottomNavItem.Settings,
        BottomNavItem.Info
    )

    var showSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Track the current filter
    val currentFilterType by viewModel.filterType.collectAsState(initial = FilterType.ALL)
    var currentFilterName by remember { mutableStateOf("All") }

    // Update filter name when filter type changes
    LaunchedEffect(currentFilterType) {
        currentFilterName = when(currentFilterType) {
            FilterType.ALL -> "All"
            FilterType.DAY -> "Day"
            FilterType.WEEK -> "Week"
            FilterType.MONTH -> "Month"
            FilterType.CATEGORY -> "Category"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            BottomNavBar(
                navController = navController,
                items = bottomNavItems
            ) { selectedItem ->
                if (selectedItem.route == BottomNavItem.Profile.route) {
                    showSheet = true
                } else {
                    navController.navigate(selectedItem.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        topBar = {
            if (showSearch) {
                // Search TopBar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* Handle search */ },
                    active = true,
                    onActiveChange = { showSearch = it },
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = {
                        IconButton(onClick = { showSearch = false }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search results will be shown here
                    // This will be populated based on searchQuery
                    val filteredTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())

                    if (searchQuery.isNotEmpty()) {
                        val results = filteredTransactions.filter {
                            it.category.contains(searchQuery, ignoreCase = true) ||
                                    it.notes.contains(searchQuery, ignoreCase = true) ||
                                    it.amount.toString().contains(searchQuery)
                        }

                        if (results.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No results found")
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(results) { transaction ->
                                    TransactionItem(transaction)
                                }
                            }
                        }
                    }
                }
            } else {
                // Regular TopBar
                TopAppBar(
                    title = { Text("Transactions") },
                    navigationIcon = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    actions = {
                        // Show the filter chip when a filter is active
                        if (currentFilterType != FilterType.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { /* Handled by trailing icon */ },
                                label = { Text(currentFilterName) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.setFilterType(FilterType.ALL)
                                            navController.navigate(BottomNavItem.Home.route)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear filter",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        FilterDropdown(LocalContext.current, navController, viewModel)
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                TransactionListScreen(viewModel)
            }
            composable("day_filter") {
                DayFilterScreen(viewModel)
            }
            composable("week_filter") {
                WeekFilterScreen(viewModel)
            }
            composable("month_filter") {
                MonthFilterScreen(viewModel)
            }
            composable("category_filter") {
                CategoryFilterScreen(viewModel)
            }
            composable(BottomNavItem.Settings.route) {
                ScreenContent("Settings Screen", Color(0xFFFFCCBC))
            }
            composable(BottomNavItem.Info.route) {
                ScreenContent("Info Screen", Color(0xFFFFAB91))
            }
        }

        if (showSheet) {
            AddTransactionBottomSheet(
                onDismiss = { showSheet = false },
                viewModel = viewModel
            )
        }
    }
}

//@Composable
//fun SearchScreen() {
//Column (modifier=Modifier.fillMaxSize().background(Color.White)){
//
//
//
//}
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    context: Context,
    navController: NavController,
    viewModel: TransactionViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("All") }

    // Keep track of filter state
    val currentFilterType by viewModel.filterType.collectAsState(initial = FilterType.ALL)

    // Update local state based on viewModel
    LaunchedEffect(currentFilterType) {
        selectedItem = when(currentFilterType) {
            FilterType.ALL -> "All"
            FilterType.DAY -> "Day"
            FilterType.WEEK -> "Week"
            FilterType.MONTH -> "Month"
            FilterType.CATEGORY -> "Category"
        }
    }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = if (selectedItem == "All")
                    Icons.Outlined.Circle
                else
                    Icons.Filled.Contrast,
                contentDescription = "Filter menu"
            )
        }
//filter selection dropdown
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            listOf("All", "Day", "Week", "Month", "Category").forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedItem = item
                        expanded = false
                        when (item) {
                            "All" -> {
                                viewModel.setFilterType(FilterType.ALL)
                                navController.navigate(BottomNavItem.Home.route)
                            }
                            "Day" -> {
                                viewModel.setFilterType(FilterType.DAY)
                                navController.navigate("day_filter")
                            }
                            "Week" -> {
                                viewModel.setFilterType(FilterType.WEEK)
                                navController.navigate("week_filter")
                            }
                            "Month" -> {
                                viewModel.setFilterType(FilterType.MONTH)
                                navController.navigate("month_filter")
                            }
                            "Category" -> {
                                viewModel.setFilterType(FilterType.CATEGORY)
                                navController.navigate("category_filter")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryFilterScreen(viewModel: TransactionViewModel) {
    var selectedCategory by remember { mutableStateOf("All") }
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currentDay = LocalDate.now()
    val currentMonth = YearMonth.now()
    val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val filteredTransactions = viewModel.filterTransactions(
        transactions,
        FilterType.CATEGORY,
        currentDay,
        currentMonth,
        currentWeekStart,
        selectedCategory
    )

    Column {
        // Segmented Button for Category
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            listOf("All", "Income", "Expense").forEachIndexed { index, category ->
                SegmentedButton(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                        viewModel.setSelectedCategory(category)
                    },
                    label = {
                        Text(category)
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions in this category",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        }
    }
}


@Composable
fun DayFilterScreen(viewModel: TransactionViewModel) {
    val currentDay by viewModel.currentDate.collectAsState(initial = LocalDate.now())
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val filteredTransactions = viewModel.filterTransactions(
        transactions,
        FilterType.DAY,
        currentDay,
        YearMonth.from(currentDay),
        currentDay.minusDays(currentDay.dayOfWeek.value.toLong() - 1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Navigation Row for Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousDay() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Day")
            }

            Text(
                text = currentDay.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { viewModel.nextDay() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Day")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions for this day.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun WeekFilterScreen(viewModel: TransactionViewModel) {
    val currentWeekStart by viewModel.currentWeekStart.collectAsState(initial = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1))
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val filteredTransactions = viewModel.filterTransactions(
        transactions,
        FilterType.WEEK,
        LocalDate.now(),
        YearMonth.from(currentWeekStart),
        currentWeekStart
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Navigation Row for Week
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousWeek() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Week")
            }

            Text(
                text = "Week of ${currentWeekStart.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { viewModel.nextWeek() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Week")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions for this week.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun MonthFilterScreen(viewModel: TransactionViewModel) {
    val currentMonth by viewModel.currentMonth.collectAsState(initial = YearMonth.now())
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    val filteredTransactions = viewModel.filterTransactions(
        transactions,
        FilterType.MONTH,
        currentMonth.atDay(1),
        currentMonth,
        currentMonth.atDay(1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Navigation Row for Month
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions for this month.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        }
    }
}





@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currentDay = LocalDate.now()
    val currentMonth = YearMonth.now()
    val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val selectedCategory by viewModel.selectedCategory.collectAsState(initial = "All")

    val filteredTransactions = viewModel.filterTransactions(
        transactions,
        FilterType.ALL,
        currentDay,
        currentMonth,
        currentWeekStart,
        selectedCategory
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Spending Summary
        SpentSummary(viewModel)



        Spacer(modifier = Modifier.height(16.dp))

        // Transactions Title
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions yet.\nClick the + button to add one.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction)
                    Divider()
                }
            }
        }
    }
}
@Composable
fun TransactionItem(transaction: Transaction) {
    val amountColor = if (transaction.type == "Income") Color.Green else Color.Black
    val amountPrefix = if (transaction.type == "Income") "+" else "-"
    val date = LocalDate.parse(transaction.date, DateTimeFormatter.ISO_LOCAL_DATE)
    val time = if (transaction.time.isNotEmpty()) {
        try {
            LocalTime.parse(transaction.time, DateTimeFormatter.ofPattern("HH:mm"))
                .format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e: Exception) {
            ""
        }
    } else ""

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
    ) {
        // Main transaction row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category indicator
                    Surface(
                        shape = CircleShape,
                        color = getCategoryColor(transaction.category),
                        modifier = Modifier.size(12.dp)
                    ) {}

                    Spacer(modifier = Modifier.width(8.dp))

                    // Transaction category and type
                    Column {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "${date.format(DateTimeFormatter.ofPattern("MMM dd"))} ${if (time.isNotEmpty()) "• $time" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "$amountPrefix$${transaction.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor
            )
        }

        // Expanded details section
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 20.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = "${date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}${
                            if (time.isNotEmpty()) " at $time" else ""
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (transaction.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    Divider(color = Color.LightGray.copy(alpha = 0.4f))
}

// Function to get color for each category (you can customize these)
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> Color(0xFFFF9800)          // Orange
        "Transport" -> Color(0xFF03A9F4)     // Light Blue
        "Shopping" -> Color(0xFFE91E63)      // Pink
        "Bills" -> Color(0xFF4CAF50)         // Green
        "Entertainment" -> Color(0xFF9C27B0) // Purple
        "Health" -> Color(0xFF00BCD4)        // Cyan
        "Salary" -> Color(0xFF4CAF50)        // Green
        "Freelance" -> Color(0xFF2196F3)     // Blue
        "Gift" -> Color(0xFFFF5722)          // Deep Orange
        "Investment" -> Color(0xFF673AB7)    // Deep Purple
        else -> Color(0xFF9E9E9E)            // Gray for Others
    }
}

@Composable
fun ScreenContent(title: String, backgroundColor: Color, contentColor: Color = Color.Black) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(text = title, color = contentColor)
        }
    }
}

@Composable
fun CategorySegmentedButton(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var selectedIndex by remember {
        mutableStateOf(
            when(selectedCategory) {
                "All" -> 0
                "Income" -> 1
                "Expense" -> 2
                else -> 0
            }
        )
    }
    val options = listOf("All", "Income", "Expense")

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        options.forEachIndexed { index, category ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    onCategorySelected(category)
                },
                icon = {}, // Optional: Add icons if needed
                label = { Text(category) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpentSummary(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val periods = listOf("Today", "This Week", "This Month", "This Year", "All Time")
    var currentPeriod by remember { mutableStateOf(periods[0]) }

    val totalSpent = remember(transactions, currentPeriod) {
        viewModel.calculateTotalSpent(transactions, currentPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var expanded by remember { mutableStateOf(false) }
        Row (Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.Center) {
            Text("Spent")
            Box() {


                TextButton(
                    onClick = { expanded = true },
//                modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentPeriod,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(80.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                    )
//                Spacer(Modifier.width(8.dp))
//                Icon(
//                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
//                    else Icons.Filled.KeyboardArrowDown,
//                    contentDescription = "Change Period"
//                )

                }
                // Period summury  Dropdown

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                currentPeriod = period
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        // Total Spent Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Text(
//                text = "Total Spent",
//                style = MaterialTheme.typography.titleMedium
//            )
            Text(
                text = "${"$%.2f".format(totalSpent)}",
                style = MaterialTheme.typography.displayLarge,
                color = Color.Black
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        setContent {
            StatusBarTestTheme {
                MainScreen(window, transactionViewModel)
            }
        }
    }
}