package com.example.statusbartest

import android.util.Log

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

enum class InsightTimeFrame {
    YEAR, MONTH, WEEK
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InsightsScreen(viewModel: TransactionViewModel, categoryViewModel: CategoryViewModel) {
    var timeFrame by remember { mutableStateOf(InsightTimeFrame.YEAR) }
    var currentYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentWeekStart by remember { mutableStateOf(
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    )}
    var menuExpanded by remember { mutableStateOf(false) }

    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Time Frame Selector with Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box {
                TextButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Text(
                        text = when (timeFrame) {
                            InsightTimeFrame.YEAR -> "Yearly"
                            InsightTimeFrame.MONTH -> "Monthly"
                            InsightTimeFrame.WEEK -> "Weekly"
                        },
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Time Frame",
                        tint = Color.Black
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Yearly") },
                        onClick = {
                            timeFrame = InsightTimeFrame.YEAR
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Monthly") },
                        onClick = {
                            timeFrame = InsightTimeFrame.MONTH
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Weekly") },
                        onClick = {
                            timeFrame = InsightTimeFrame.WEEK
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Period navigation and title
        when (timeFrame) {
            InsightTimeFrame.YEAR -> YearInsights(
                transactions = transactions,
                currentYear = currentYear,
                onPreviousYear = { currentYear-- },
                onNextYear = {
                    if (currentYear < LocalDate.now().year) currentYear++
                }
            )

            InsightTimeFrame.MONTH -> MonthInsights(
                transactions = transactions,
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = {
                    if (currentMonth.isBefore(YearMonth.now()) ||
                        currentMonth.equals(YearMonth.now())) {
                        currentMonth = currentMonth.plusMonths(1)
                    }
                }
            )

            InsightTimeFrame.WEEK -> WeekInsights(
                transactions = transactions,
                currentWeekStart = currentWeekStart,
                onPreviousWeek = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                onNextWeek = {
                    val nextWeekStart = currentWeekStart.plusWeeks(1)
                    if (nextWeekStart.isBefore(LocalDate.now()) ||
                        nextWeekStart.equals(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))) {
                        currentWeekStart = nextWeekStart
                    }
                }
            )
        }
    }
}

@Composable
fun TimeFrameNavigator(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    enableNext: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Period",
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onNext,
            enabled = enableNext
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Period",
                modifier = Modifier.size(32.dp),
                tint = if (enableNext) Color.Black else Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun YearInsights(
    transactions: List<Transaction>,
    currentYear: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit
) {
    // Handle swipe gestures for year navigation
    var dragAmount by remember { mutableStateOf(0f) }
    val enableNextYear = currentYear < LocalDate.now().year

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragAmount > 100) {
                            onPreviousYear()
                        } else if (dragAmount < -100 && enableNextYear) {
                            onNextYear()
                        }
                        dragAmount = 0f
                    },
                    onDrag = { change, dragAmountt ->
                        dragAmount += dragAmountt.x
                        change.consume()
                    }
                )
            }
    ) {
        // Year Navigator
        TimeFrameNavigator(
            title = "$currentYear",
            onPrevious = onPreviousYear,
            onNext = onNextYear,
            enableNext = enableNextYear
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter transactions for current year
        val yearTransactions = transactions.filter {
            try {
                val date = LocalDate.parse(it.date)
                date.year == currentYear
            } catch (e: Exception) {
                false
            }
        }

        // Calculate year summary
        val yearIncome = yearTransactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }

        val yearExpenses = yearTransactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }

        val yearBalance = yearIncome - yearExpenses

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = yearIncome,
                positive = true,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Expenses",
                amount = yearExpenses,
                positive = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(
            title = "Balance",
            amount = yearBalance,
            positive = yearBalance >= 0,
            modifier = Modifier.fillMaxWidth(),
            large = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Monthly data for the chart
        val monthlyData = (1..12).map { month ->
            val monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val monthTransactions = yearTransactions.filter {
                try {
                    val date = LocalDate.parse(it.date)
                    date.month.value == month
                } catch (e: Exception) {
                    false
                }
            }

            val income = monthTransactions
                .filter { it.type == "Income" }
                .sumOf { it.amount }

            val expenses = monthTransactions
                .filter { it.type == "Expense" }
                .sumOf { it.amount }

            monthName to (income - expenses)
        }

        // Chart title
        Text(
            text = "Monthly Balance",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Line Chart
        LineChart(
            data = monthlyData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Top Categories
        CategoryBreakdown(
            transactions = yearTransactions,
            title = "Top Categories",
            limit = 5
        )
    }
}

@Composable
fun MonthInsights(
    transactions: List<Transaction>,
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    // Handle swipe gestures for month navigation
    var dragAmount by remember { mutableStateOf(0f) }
    val enableNextMonth = currentMonth.isBefore(YearMonth.now()) ||
            currentMonth.equals(YearMonth.now())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragAmount > 100) {
                            onPreviousMonth()
                        } else if (dragAmount < -100 && enableNextMonth) {
                            onNextMonth()
                        }
                        dragAmount = 0f
                    },
                    onDrag = { change, dragAmountt ->
                        dragAmount += dragAmountt.x
                        change.consume()
                    }
                )
            }
    ) {
        // Month Navigator
        TimeFrameNavigator(
            title = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
            enableNext = enableNextMonth
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter transactions for current month
        val monthTransactions = transactions.filter {
            try {
                val date = LocalDate.parse(it.date)
                YearMonth.from(date).equals(currentMonth)
            } catch (e: Exception) {
                false
            }
        }

        // Calculate month summary
        val monthIncome = monthTransactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }

        val monthExpenses = monthTransactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }

        val monthBalance = monthIncome - monthExpenses

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = monthIncome,
                positive = true,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Expenses",
                amount = monthExpenses,
                positive = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(
            title = "Balance",
            amount = monthBalance,
            positive = monthBalance >= 0,
            modifier = Modifier.fillMaxWidth(),
            large = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Daily data for the chart
        val daysInMonth = currentMonth.lengthOfMonth()
        val dailyData = (1..daysInMonth).map { day ->
            val dayDate = currentMonth.atDay(day)
            val dayTransactions = monthTransactions.filter {
                try {
                    val date = LocalDate.parse(it.date)
                    date.equals(dayDate)
                } catch (e: Exception) {
                    false
                }
            }

            val income = dayTransactions
                .filter { it.type == "Income" }
                .sumOf { it.amount }

            val expenses = dayTransactions
                .filter { it.type == "Expense" }
                .sumOf { it.amount }

            day.toString() to (income - expenses)
        }

        // Chart title
        Text(
            text = "Daily Balance",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Line Chart
        LineChart(
            data = dailyData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp),
            displayEveryNthLabel = if (daysInMonth > 15) 5 else 1 // Only show every 5th label if too many days
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Top Categories
        CategoryBreakdown(
            transactions = monthTransactions,
            title = "Top Categories",
            limit = 5
        )
    }
}

@Composable
fun WeekInsights(
    transactions: List<Transaction>,
    currentWeekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    // Calculate week end
    val currentWeekEnd = currentWeekStart.plusDays(6)
    val weekDateFormat = DateTimeFormatter.ofPattern("MMM d")

    // Handle swipe gestures for week navigation
    var dragAmount by remember { mutableStateOf(0f) }
    val todayWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val enableNextWeek = currentWeekStart.isBefore(todayWeekStart) ||
            currentWeekStart.equals(todayWeekStart)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragAmount > 100) {
                            onPreviousWeek()
                        } else if (dragAmount < -100 && enableNextWeek) {
                            onNextWeek()
                        }
                        dragAmount = 0f
                    },
                    onDrag = { change, dragAmountt ->
                        dragAmount += dragAmountt.x
                        change.consume()
                    }
                )
            }
    ) {
        // Week Navigator
        TimeFrameNavigator(
            title = "${currentWeekStart.format(weekDateFormat)} - ${currentWeekEnd.format(weekDateFormat)}",
            onPrevious = onPreviousWeek,
            onNext = onNextWeek,
            enableNext = enableNextWeek
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter transactions for current week
        val weekTransactions = transactions.filter {
            try {
                val date = LocalDate.parse(it.date)
                (date.isEqual(currentWeekStart) || date.isAfter(currentWeekStart)) &&
                        (date.isEqual(currentWeekEnd) || date.isBefore(currentWeekEnd))
            } catch (e: Exception) {
                false
            }
        }

        // Calculate week summary
        val weekIncome = weekTransactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }

        val weekExpenses = weekTransactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }

        val weekBalance = weekIncome - weekExpenses

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = weekIncome,
                positive = true,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Expenses",
                amount = weekExpenses,
                positive = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(
            title = "Balance",
            amount = weekBalance,
            positive = weekBalance >= 0,
            modifier = Modifier.fillMaxWidth(),
            large = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Daily data for the chart
        val dailyData = (0..6).map { dayOffset ->
            val dayDate = currentWeekStart.plusDays(dayOffset.toLong())
            val dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            val dayTransactions = weekTransactions.filter {
                try {
                    val date = LocalDate.parse(it.date)
                    date.equals(dayDate)
                } catch (e: Exception) {
                    false
                }
            }

            val income = dayTransactions
                .filter { it.type == "Income" }
                .sumOf { it.amount }

            val expenses = dayTransactions
                .filter { it.type == "Expense" }
                .sumOf { it.amount }

            dayName to (income - expenses)
        }

        // Chart title
        Text(
            text = "Daily Balance",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Line Chart
        LineChart(
            data = dailyData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Top Categories
        CategoryBreakdown(
            transactions = weekTransactions,
            title = "Top Categories",
            limit = 5
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    positive: Boolean,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val backgroundColor = when {
        title == "Balance" && positive -> Color(0xE8F5E9FF) // Light green
        title == "Balance" && !positive -> Color(0xFFEBEE) // Light red
        title == "Income" -> Color(0xE8F5E9FF) // Light green
        title == "Expenses" -> Color(0xFAFAFAFF) // Light gray
        else -> Color.White
    }

    val textColor = when {
        positive -> Color(0xFF4CAF50) // Green
        title == "Expenses" -> Color.Black
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${String.format("%.2f", abs(amount))}",
                style = if (large)
                    MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    displayEveryNthLabel: Int = 1
) {
    // If no data, show a placeholder
    if (data.isEmpty() || data.all { it.second == 0.0 }) {
        Box(
            modifier = modifier
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available for this period",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Determine data range for scaling
    val values = data.map { it.second }
    val maxValue = values.maxOrNull() ?: 0.0
    val minValue = values.minOrNull() ?: 0.0
    val absMax = maxOf(abs(maxValue), abs(minValue))

    // Ensure we have a reasonable range
    val effectiveMax = maxOf(absMax, 1.0)

    // Box to contain chart
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 16.dp,
                bottom = 32.dp // Space for labels
            )
    ) {
        // Draw the zero line
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )

        // The actual chart
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, (label, value) ->
                // Skip negative values to prevent issues with bar drawing
                val normalizedValue = (value / effectiveMax * 0.8).coerceIn(-0.8, 0.8)
                val barHeight = (abs(normalizedValue) * 100).toInt().dp
                val isPositive = value >= 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isPositive) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(barHeight)
                                .background(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(barHeight)
                                .background(
                                    color = Color(0xFFF44336).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                                )
                        )
                    }

                    // X-axis labels (only show every Nth label if there are many)
                    if (index % displayEveryNthLabel == 0) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 10.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdown(
    transactions: List<Transaction>,
    title: String,
    limit: Int = 5
) {
    // Count by category
    val categoryCounts = transactions.groupBy { it.category }
        .mapValues { (_, transactions) -> transactions.size }
        .toList()
        .sortedByDescending { it.second }
        .take(limit)

    // Sum by category
    val categorySums = transactions.groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.sumOf {
                if (it.type == "Expense") it.amount else 0.0
            }
        }
        .toList()
        .sortedByDescending { it.second }
        .take(limit)

    if (categoryCounts.isEmpty()) {
        return // No categories to show
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Most Used Categories
        Text(
            text = "Most Used Categories",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        categoryCounts.forEach { (category, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$count transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Highest Spending Categories
        Text(
            text = "Highest Spending Categories",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        categorySums.forEach { (category, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}