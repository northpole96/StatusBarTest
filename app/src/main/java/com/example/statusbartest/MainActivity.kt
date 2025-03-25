package com.example.statusbartest


import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate
import java.time.YearMonth
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
    NavigationBar {
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
fun CustomDigitKeyboard(input: String, onInputChange: (String) -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "Del"),
        listOf("Clear")
    )

    Column {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
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
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }
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
    val context = LocalContext.current

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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$input",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomDigitKeyboard(input = input, onInputChange = { input = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Button
            Button(onClick = {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    },
                    selectedDate.year,
                    selectedDate.monthValue - 1,
                    selectedDate.dayOfMonth
                )
                datePickerDialog.show()
            }) {
                Text("Date: $selectedDate")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction Type Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Type: ")
                Spacer(modifier = Modifier.width(8.dp))

                val options = listOf("Expense", "Income")
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = transactionType == option,
                            onClick = { transactionType = option }
                        )
                        Text(option)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (input.isNotEmpty()) {
                    try {
                        // Save transaction to database
                        viewModel.insert(
                            amount = input.toDouble(),
                            type = transactionType,
                            date = selectedDate
                        )
                        onDismiss()
                    } catch (e: NumberFormatException) {
                        // Handle invalid input
                    }
                }
            }) {
                Text("Save Transaction")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(window: Window, viewModel: TransactionViewModel) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Profile,
        BottomNavItem.Settings,
        BottomNavItem.Info
    )

    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
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
//        floatingActionButton = {
//            FloatingActionButton(onClick = { showSheet = true }) {
//                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
//            }
//        },
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    FilterDropdown(LocalContext.current, navController)
                }
            )
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
            composable(BottomNavItem.Search.route) {
//                ScreenContent("Search Screen", Color(0xFFFFF9C4))
           SearchScreen()
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
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("All") }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = if (selectedItem == "All")
                    Icons.Outlined.ArrowDropDown
                else
                    Icons.Filled.ArrowDropDown,
                contentDescription = "Filter menu"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("All", "Day", "Week", "Month", "Category").forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedItem = item
                        expanded = false
                        when (item) {
                            "All" -> navController.navigate(BottomNavItem.Home.route)
                            "Day" -> navController.navigate("day_filter")
                            "Week" -> navController.navigate("week_filter")
                            "Month" -> navController.navigate("month_filter")
                            "Category" -> navController.navigate("category_filter")
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
            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
    val amountColor = if (transaction.type == "Income") Color.Green else Color.Red
    val amountPrefix = if (transaction.type == "Income") "+" else "-"
    val date = LocalDate.parse(transaction.date, DateTimeFormatter.ISO_LOCAL_DATE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = transaction.type,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Text(
            text = "$amountPrefix$${transaction.amount}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
        // Period Dropdown
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
                        style = MaterialTheme.typography.titleMedium
                    )
//                Spacer(Modifier.width(8.dp))
//                Icon(
//                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
//                    else Icons.Filled.KeyboardArrowDown,
//                    contentDescription = "Change Period"
//                )

                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
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