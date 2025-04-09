package com.example.statusbartest

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class InsightTimeFrame {
    WEEK, MONTH, YEAR
}

enum class TransactionViewMode {
    INCOME, EXPENSE, BOTH
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
) {
    // State management
    var timeFrame by remember { mutableStateOf(InsightTimeFrame.MONTH) }
    var viewMode by remember { mutableStateOf(TransactionViewMode.BOTH) }

    // Date navigation state
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())

    // Store drag offset for swipe navigation
    var dragOffset by remember { mutableStateOf(0f) }
    // Boolean to track if animation is in progress
    var isAnimating by remember { mutableStateOf(false) }
    // Coroutine scope for launching animations
    val coroutineScope = rememberCoroutineScope()

    // Derived period-specific dates based on selected time frame
    val periodStart = remember(currentDate, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            InsightTimeFrame.MONTH -> currentDate.withDayOfMonth(1)
            InsightTimeFrame.YEAR -> currentDate.withDayOfMonth(1).withMonth(1)
        }
    }

    val periodEnd = remember(periodStart, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> periodStart.plusDays(6) // Monday to Sunday
            InsightTimeFrame.MONTH -> periodStart.plusMonths(1).minusDays(1) // Last day of month
            InsightTimeFrame.YEAR -> periodStart.plusYears(1).minusDays(1) // Last day of year
        }
    }

    // Previous period for comparison
    val previousPeriodStart = remember(periodStart, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> periodStart.minusWeeks(1)
            InsightTimeFrame.MONTH -> periodStart.minusMonths(1)
            InsightTimeFrame.YEAR -> periodStart.minusYears(1)
        }
    }

    val previousPeriodEnd = remember(previousPeriodStart, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> previousPeriodStart.plusDays(6)
            InsightTimeFrame.MONTH -> previousPeriodStart.plusMonths(1).minusDays(1)
            InsightTimeFrame.YEAR -> previousPeriodStart.plusYears(1).minusDays(1)
        }
    }

    // Filter transactions for current period
    val currentPeriodTransactions = remember(allTransactions, periodStart, periodEnd) {
        allTransactions.filter { transaction ->
            try {
                val transactionDate = LocalDate.parse(transaction.date)
                !transactionDate.isBefore(periodStart) && !transactionDate.isAfter(periodEnd)
            } catch (e: Exception) {
                false
            }
        }
    }

    // Filter transactions for previous period
    val previousPeriodTransactions =
        remember(allTransactions, previousPeriodStart, previousPeriodEnd) {
            allTransactions.filter { transaction ->
                try {
                    val transactionDate = LocalDate.parse(transaction.date)
                    !transactionDate.isBefore(previousPeriodStart) && !transactionDate.isAfter(
                        previousPeriodEnd
                    )
                } catch (e: Exception) {
                    false
                }
            }
        }

    // Calculate income and expense totals
    val currentIncome = remember(currentPeriodTransactions) {
        currentPeriodTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    }

    val currentExpense = remember(currentPeriodTransactions) {
        currentPeriodTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
    }

    val previousIncome = remember(previousPeriodTransactions) {
        previousPeriodTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    }

    val previousExpense = remember(previousPeriodTransactions) {
        previousPeriodTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
    }

    // Calculate percentage changes
    val incomeChange = remember(currentIncome, previousIncome) {
        if (previousIncome > 0) {
            ((currentIncome - previousIncome) / previousIncome * 100).roundToInt()
        } else if (currentIncome > 0) {
            100 // If previous was 0 and current is positive, show 100% increase
        } else {
            0 // If both are 0, no change
        }
    }

    val expenseChange = remember(currentExpense, previousExpense) {
        if (previousExpense > 0) {
            ((currentExpense - previousExpense) / previousExpense * 100).roundToInt()
        } else if (currentExpense > 0) {
            100 // If previous was 0 and current is positive, show 100% increase
        } else {
            0 // If both are 0, no change
        }
    }

    // Format period title based on timeFrame
    val periodTitle = remember(periodStart, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                val weekEndDate = periodStart.plusDays(6)
                "${periodStart.format(DateTimeFormatter.ofPattern("MMM d"))} - ${
                    weekEndDate.format(
                        DateTimeFormatter.ofPattern("MMM d, yyyy")
                    )
                }"
            }

            InsightTimeFrame.MONTH -> periodStart.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            InsightTimeFrame.YEAR -> periodStart.format(DateTimeFormatter.ofPattern("yyyy"))
        }
    }

    // Handle period navigation
    val navigateToPreviousPeriod = {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> currentDate = currentDate.minusWeeks(1)
            InsightTimeFrame.MONTH -> currentDate = currentDate.minusMonths(1)
            InsightTimeFrame.YEAR -> currentDate = currentDate.minusYears(1)
        }
    }


    val navigateToNextPeriod = {
        if (!isAnimating) {
            val today = LocalDate.now()
            // Don't navigate past today
            val wouldExceedToday = when (timeFrame) {
                InsightTimeFrame.WEEK -> periodStart.plusWeeks(1).isAfter(today)
                InsightTimeFrame.MONTH -> periodStart.plusMonths(1).isAfter(today)
                InsightTimeFrame.YEAR -> periodStart.plusYears(1).isAfter(today)
            }

            if (!wouldExceedToday) {
                isAnimating = true
                when (timeFrame) {
                    InsightTimeFrame.WEEK -> currentDate = currentDate.plusWeeks(1)
                    InsightTimeFrame.MONTH -> currentDate = currentDate.plusMonths(1)
                    InsightTimeFrame.YEAR -> currentDate = currentDate.plusYears(1)
                }
                coroutineScope.launch {
                    // Reset drag offset with animation
                    dragOffset = 0f
                    isAnimating = false
                }
            }
        }
    }

    // UI Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    InsightsTimePeriodDropdown(
                        selectedTimeFrame = timeFrame,
                        onTimeFrameSelected = {
                            timeFrame = it
                            // Reset date to current when changing time frame
                            currentDate = LocalDate.now()
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .pointerInput(timeFrame) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Determine if drag should result in navigation
                            val threshold = size.width * 0.2f // 20% of screen width

                            if (dragOffset > threshold) {
                                // Swiped right - go to previous period
                                navigateToPreviousPeriod()
                            } else if (dragOffset < -threshold) {
                                // Swiped left - go to next period
                                navigateToNextPeriod()
                            }

                            // Reset drag offset
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            // Reset drag offset
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // Update drag offset based on user's finger movement
                            dragOffset += dragAmount

                            // Limit the maximum drag distance
                            val maxDragDistance = size.width * 0.4f
                            dragOffset = dragOffset.coerceIn(-maxDragDistance, maxDragDistance)
                        }
                    )
                }
                .verticalScroll(rememberScrollState())
        ) {
            // Period Navigation with swipe indication
            Box(modifier = Modifier.fillMaxWidth()) {
                // Swipe indicator (subtle visual cue)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (dragOffset != 0f) abs(dragOffset).dp else 0.dp),
                    horizontalArrangement = if (dragOffset > 0) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dragOffset > 0f) {
                        // Swiping right (previous period)
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = min((dragOffset / 200f), 0.5f)),
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (dragOffset < 0f) {
                        // Swiping left (next period)
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.LightGray.copy(
                                alpha = min(
                                    (abs(dragOffset) / 200f),
                                    0.5f
                                )
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Main navigation controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = navigateToPreviousPeriod) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous period"
                        )
                    }

                    Text(
                        text = periodTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Disable next button if at current period
                    val isCurrentPeriod = when (timeFrame) {
                        InsightTimeFrame.WEEK -> {
                            val thisWeekStart = LocalDate.now()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            periodStart.isEqual(thisWeekStart)
                        }

                        InsightTimeFrame.MONTH -> {
                            val thisMonthStart = LocalDate.now().withDayOfMonth(1)
                            periodStart.isEqual(thisMonthStart)
                        }

                        InsightTimeFrame.YEAR -> {
                            val thisYearStart = LocalDate.now().withDayOfMonth(1).withMonth(1)
                            periodStart.isEqual(thisYearStart)
                        }
                    }

                    IconButton(
                        onClick = navigateToNextPeriod,
                        enabled = !isCurrentPeriod
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next period",
                            tint = if (isCurrentPeriod) Color.Gray.copy(alpha = 0.5f) else LocalContentColor.current
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income/Expense Toggle
            TransactionViewModeToggle(
                selectedMode = viewMode,
                onModeSelected = { viewMode = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Income Summary Card (shown if viewMode is INCOME or BOTH)
            if (viewMode == TransactionViewMode.INCOME || viewMode == TransactionViewMode.BOTH) {
                SummaryCard(
                    title = "Income",
                    amount = currentIncome,
                    percentChange = incomeChange,
                    isPositiveGood = true,
                    color = Color(0xFF4CAF50) // Green
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expense Summary Card (shown if viewMode is EXPENSE or BOTH)
            if (viewMode == TransactionViewMode.EXPENSE || viewMode == TransactionViewMode.BOTH) {
                SummaryCard(
                    title = "Expenses",
                    amount = currentExpense,
                    percentChange = expenseChange,
                    isPositiveGood = false,
                    color = Color(0xFF666666) // Dark gray
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Balance Card (shown only in BOTH view)
            if (viewMode == TransactionViewMode.BOTH) {
                SummaryCard(
                    title = "Balance",
                    amount = currentIncome - currentExpense,
                    percentChange = null, // No percentage for balance
                    color = if (currentIncome >= currentExpense) Color(0xFF4CAF50) else Color(
                        0xFFE57373
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Charts section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    when (viewMode) {
                        TransactionViewMode.INCOME -> {
                            TransactionChart(
                                transactions = currentPeriodTransactions.filter { it.type == "Income" },
                                timeFrame = timeFrame,
                                periodStart = periodStart,
                                periodEnd = periodEnd,
                                color = Color(0xFF4CAF50),
                                height = 240.dp
                            )
                        }

                        TransactionViewMode.EXPENSE -> {
                            TransactionChart(
                                transactions = currentPeriodTransactions.filter { it.type == "Expense" },
                                timeFrame = timeFrame,
                                periodStart = periodStart,
                                periodEnd = periodEnd,
                                color = Color(0xFF666666),
                                height = 240.dp
                            )
                        }

                        TransactionViewMode.BOTH -> {
                            // Show both income and expense on the same chart
                            DualTransactionChart(
                                incomeTransactions = currentPeriodTransactions.filter { it.type == "Income" },
                                expenseTransactions = currentPeriodTransactions.filter { it.type == "Expense" },
                                timeFrame = timeFrame,
                                periodStart = periodStart,
                                periodEnd = periodEnd,
                                incomeColor = Color(0xFF4CAF50),
                                expenseColor = Color(0xFF666666),
                                height = 240.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category breakdown
            when (viewMode) {
                TransactionViewMode.INCOME -> {
                    CategoryBreakdownCard(
                        transactions = currentPeriodTransactions.filter { it.type == "Income" },
                        title = "Income by Category",
                        categoryViewModel = categoryViewModel,
                        timeFrame = timeFrame
                    )
                }

                TransactionViewMode.EXPENSE -> {
                    CategoryBreakdownCard(
                        transactions = currentPeriodTransactions.filter { it.type == "Expense" },
                        title = "Expenses by Category",
                        categoryViewModel = categoryViewModel,
                        timeFrame = timeFrame
                    )
                }

                TransactionViewMode.BOTH -> {
                    // Show expense breakdown by default when in BOTH mode
                    CategoryBreakdownCard(
                        transactions = currentPeriodTransactions.filter { it.type == "Expense" },
                        title = "Expenses by Category",
                        categoryViewModel = categoryViewModel,
                        timeFrame = timeFrame
                    )
                }
            }
        }
    }
}

@Composable
fun InsightsTimePeriodDropdown(
    selectedTimeFrame: InsightTimeFrame,
    onTimeFrameSelected: (InsightTimeFrame) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true }
        ) {
            Text(
                text = when (selectedTimeFrame) {
                    InsightTimeFrame.WEEK -> "Week"
                    InsightTimeFrame.MONTH -> "Month"
                    InsightTimeFrame.YEAR -> "Year"
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select time period"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            listOf(
                InsightTimeFrame.WEEK to "Week",
                InsightTimeFrame.MONTH to "Month",
                InsightTimeFrame.YEAR to "Year"
            ).forEach { (timeFrame, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTimeFrameSelected(timeFrame)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionViewModeToggle(
    selectedMode: TransactionViewMode,
    onModeSelected: (TransactionViewMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedMode == TransactionViewMode.BOTH,
            onClick = { onModeSelected(TransactionViewMode.BOTH) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color.Black,
                activeContentColor = Color.White
            ),
            label = { Text("Both") }
        )

        SegmentedButton(
            selected = selectedMode == TransactionViewMode.INCOME,
            onClick = { onModeSelected(TransactionViewMode.INCOME) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color.Black,
                activeContentColor = Color.White
            ),
            label = { Text("Income") }
        )

        SegmentedButton(
            selected = selectedMode == TransactionViewMode.EXPENSE,
            onClick = { onModeSelected(TransactionViewMode.EXPENSE) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color.Black,
                activeContentColor = Color.White
            ),
            label = { Text("Expense") }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    percentChange: Int? = null,
    isPositiveGood: Boolean = true,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )

                if (percentChange != null) {
                    val isPositive = percentChange > 0
                    val changeColor = when {
                        percentChange == 0 -> Color.Gray
                        (isPositive && isPositiveGood) || (!isPositive && !isPositiveGood) -> Color(
                            0xFF4CAF50
                        )

                        else -> Color(0xFFE57373)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = changeColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                            contentDescription = if (isPositive) "Increase" else "Decrease",
                            tint = changeColor,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${if (isPositive) "+" else ""}$percentChange%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = changeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionChart(
    transactions: List<Transaction>,
    timeFrame: InsightTimeFrame,
    periodStart: LocalDate,
    periodEnd: LocalDate,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    // Prepare data points based on time frame
    val dataPoints = remember(transactions, timeFrame, periodStart, periodEnd) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                // Group by day of week
                val dayTotals = Array(7) { 0.0 }
                transactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        val dayIndex = ChronoUnit.DAYS.between(periodStart, date).toInt()
                        if (dayIndex in 0..6) {
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.MONTH -> {
                // Group by day of month
                val daysInMonth = periodStart.lengthOfMonth()
                val dayTotals = Array(daysInMonth) { 0.0 }
                transactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.month == periodStart.month && date.year == periodStart.year) {
                            val dayIndex = date.dayOfMonth - 1
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.YEAR -> {
                // Group by month
                val monthTotals = Array(12) { 0.0 }
                transactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.year == periodStart.year) {
                            val monthIndex = date.monthValue - 1
                            monthTotals[monthIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                monthTotals.toList()
            }
        }
    }

    // Calculate max value for scaling
    val maxValue = remember(dataPoints) {
        dataPoints.maxOrNull() ?: 0.0
    }

    // X-axis labels
    val xLabels = remember(timeFrame, periodStart) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                (0..6).map {
                    periodStart.plusDays(it.toLong())
                        .dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            Locale.getDefault()
                        )
                }
            }

            InsightTimeFrame.MONTH -> {
                val daysInMonth = periodStart.lengthOfMonth()
                (1..daysInMonth).map { it.toString() }
            }

            InsightTimeFrame.YEAR -> {
                (1..12).map {
                    Month.of(it)
                        .getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // If no data or all zeros, show empty state
        if (transactions.isEmpty() || maxValue <= 0.0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data for this period",
                    color = Color.Gray
                )
            }
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val usableHeight = height * 0.85f // Leave room for labels

            // Draw the x-axis line
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, usableHeight),
                end = Offset(width, usableHeight),
                strokeWidth = 1.5f
            )

            // Calculate bar width and spacing
            val totalBars = dataPoints.size
            val barWidth = (width / totalBars) * 0.7f
            val barSpacing = (width / totalBars) * 0.3f

            // Draw data bars
            dataPoints.forEachIndexed { index, value ->
                if (value > 0) {
                    val barHeight = (value / maxValue) * usableHeight
                    val startX = index * (barWidth + barSpacing) + barSpacing / 2

                    drawRect(
                        color = color.copy(alpha = 0.7f),
//                        topLeft = Offset(startX, usableHeight - barHeight),
//                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                }
            }
        }

        // Draw X-axis labels
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Show a subset of labels to avoid overcrowding
            val labelStep = when (timeFrame) {
                InsightTimeFrame.WEEK -> 1 // Show all days
                InsightTimeFrame.MONTH -> if (xLabels.size > 15) 5 else 3
                InsightTimeFrame.YEAR -> 1 // Show all months
            }

            // Display a subset of labels to avoid overcrowding
            val visibleLabels = when (timeFrame) {
                InsightTimeFrame.WEEK -> xLabels // All week days
                InsightTimeFrame.MONTH -> xLabels.filterIndexed { index, _ -> index % labelStep == 0 }
                InsightTimeFrame.YEAR -> xLabels // All months
            }

            visibleLabels.forEach { label ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center,
//                    modifier = Modifier.width(width = (Modifier.fillMaxWidth().then(Modifier.width(0.dp))).width / visibleLabels.size)
                )
            }
        }
    }
}

@Composable
fun DualTransactionChart(
    incomeTransactions: List<Transaction>,
    expenseTransactions: List<Transaction>,
    timeFrame: InsightTimeFrame,
    periodStart: LocalDate,
    periodEnd: LocalDate,
    incomeColor: Color,
    expenseColor: Color,
    height: androidx.compose.ui.unit.Dp
) {
    // Prepare income data points
    val incomeDataPoints = remember(incomeTransactions, timeFrame, periodStart, periodEnd) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                val dayTotals = Array(7) { 0.0 }
                incomeTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        val dayIndex = ChronoUnit.DAYS.between(periodStart, date).toInt()
                        if (dayIndex in 0..6) {
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.MONTH -> {
                val daysInMonth = periodStart.lengthOfMonth()
                val dayTotals = Array(daysInMonth) { 0.0 }
                incomeTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.month == periodStart.month && date.year == periodStart.year) {
                            val dayIndex = date.dayOfMonth - 1
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.YEAR -> {
                val monthTotals = Array(12) { 0.0 }
                incomeTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.year == periodStart.year) {
                            val monthIndex = date.monthValue - 1
                            monthTotals[monthIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                monthTotals.toList()
            }
        }
    }

    // Prepare expense data points
    val expenseDataPoints = remember(expenseTransactions, timeFrame, periodStart, periodEnd) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                val dayTotals = Array(7) { 0.0 }
                expenseTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        val dayIndex = ChronoUnit.DAYS.between(periodStart, date).toInt()
                        if (dayIndex in 0..6) {
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.MONTH -> {
                val daysInMonth = periodStart.lengthOfMonth()
                val dayTotals = Array(daysInMonth) { 0.0 }
                expenseTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.month == periodStart.month && date.year == periodStart.year) {
                            val dayIndex = date.dayOfMonth - 1
                            dayTotals[dayIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                dayTotals.toList()
            }

            InsightTimeFrame.YEAR -> {
                val monthTotals = Array(12) { 0.0 }
                expenseTransactions.forEach { transaction ->
                    try {
                        val date = LocalDate.parse(transaction.date)
                        if (date.year == periodStart.year) {
                            val monthIndex = date.monthValue - 1
                            monthTotals[monthIndex] += transaction.amount
                        }
                    } catch (e: Exception) { /* Skip invalid dates */
                    }
                }
                monthTotals.toList()
            }
        }
    }

    // Calculate max value for scaling
    val maxValue = remember(incomeDataPoints, expenseDataPoints) {
        maxOf(
            incomeDataPoints.maxOrNull() ?: 0.0,
            expenseDataPoints.maxOrNull() ?: 0.0
        )
    }

    // X-axis labels
    val xLabels = remember(timeFrame, periodStart) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> {
                (0..6).map {
                    periodStart.plusDays(it.toLong())
                        .dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            Locale.getDefault()
                        )
                }
            }

            InsightTimeFrame.MONTH -> {
                val daysInMonth = periodStart.lengthOfMonth()
                (1..daysInMonth).map { it.toString() }
            }

            InsightTimeFrame.YEAR -> {
                (1..12).map {
                    Month.of(it)
                        .getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // If no data or all zeros, show empty state
        if ((incomeTransactions.isEmpty() && expenseTransactions.isEmpty()) || maxValue <= 0.0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data for this period",
                    color = Color.Gray
                )
            }
            return@Column
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val usableHeight = height * 0.85f // Leave room for labels

                // Draw the x-axis line
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, usableHeight),
                    end = Offset(width, usableHeight),
                    strokeWidth = 1.5f
                )

                // Calculate bar width and spacing
                val totalBars = incomeDataPoints.size
                val barGroupWidth = (width / totalBars)
                val barWidth = barGroupWidth * 0.35f
                val barSpacing = barGroupWidth * 0.3f

                // Draw income bars
                incomeDataPoints.forEachIndexed { index, value ->
                    if (value > 0) {
                        val barHeight = (value / maxValue) * usableHeight
                        val startX = index * barGroupWidth + barSpacing / 2

                        drawRect(
                            color = incomeColor.copy(alpha = 0.7f),
//                            topLeft = Offset(startX, usableHeight - barHeight),
//                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }

                // Draw expense bars
                expenseDataPoints.forEachIndexed { index, value ->
                    if (value > 0) {
                        val barHeight = (value / maxValue) * usableHeight
                        val startX =
                            index * barGroupWidth + barSpacing / 2 + barWidth + barSpacing * 0.3f

                        drawRect(
                            color = expenseColor.copy(alpha = 0.7f),
//                            topLeft = Offset(startX, usableHeight - barHeight),
//                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Show a subset of labels to avoid overcrowding
            val labelStep = when (timeFrame) {
                InsightTimeFrame.WEEK -> 1 // Show all days
                InsightTimeFrame.MONTH -> if (xLabels.size > 15) 5 else 3
                InsightTimeFrame.YEAR -> 1 // Show all months
            }

            val visibleLabels = when (timeFrame) {
                InsightTimeFrame.WEEK -> xLabels // All week days
                InsightTimeFrame.MONTH -> xLabels.filterIndexed { index, _ -> index % labelStep == 0 }
                InsightTimeFrame.YEAR -> xLabels // All months
            }

            visibleLabels.forEach { label ->
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center,
//                    modifier = Modifier.width(width = (Modifier.fillMaxWidth().then(Modifier.width(0.dp))).width() / visibleLabels.size)
                )
            }
        }

        // Chart legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Income legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(incomeColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Income",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                )
            }

            // Expense legend
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(expenseColor.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expenses",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    transactions: List<Transaction>,
    title: String,
    categoryViewModel: CategoryViewModel,
    timeFrame: InsightTimeFrame
) {
    if (transactions.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transaction data available for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Group transactions by category
    val categoryTotals = remember(transactions) {
        transactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { (_, total) -> total }
    }

    // Get total amount
    val totalAmount = remember(categoryTotals) {
        categoryTotals.sumOf { (_, amount) -> amount }
    }

    // Get category objects
    val isExpenseType = transactions.firstOrNull()?.type == "Expense"
    val categories by if (isExpenseType) {
        categoryViewModel.expenseDefaultCategories.collectAsState(initial = emptyList())
    } else {
        categoryViewModel.incomeDefaultCategories.collectAsState(initial = emptyList())
    }

    val customCategories by if (isExpenseType) {
        categoryViewModel.expenseCustomCategories.collectAsState(initial = emptyList())
    } else {
        categoryViewModel.incomeCustomCategories.collectAsState(initial = emptyList())
    }

    val allCategories = remember(categories, customCategories) {
        categories + customCategories
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            categoryTotals.take(5).forEach { (category, amount) -> // Show top 5 categories
                val percentage = (amount / totalAmount * 100).toInt()
                val categoryObject = allCategories.find { it.name == category }
                val categoryColor = categoryObject?.let {
                    try {
                        Color(android.graphics.Color.parseColor(it.colorHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                } ?: Color.Gray

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category name with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (categoryObject != null) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(categoryColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = categoryObject.emoji,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Amount and percentage
                        Row {
                            Text(
                                text = "${String.format("%.2f", amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Color.LightGray.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(percentage / 100f)
                                .background(categoryColor, RoundedCornerShape(2.dp))
                        )
                    }
                }

                if (categoryTotals.indexOf(category to amount) < categoryTotals.size - 1 &&
                    categoryTotals.indexOf(category to amount) < 4
                ) { // Don't add divider after last item
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }

            // Show "Others" category if there are more categories
            if (categoryTotals.size > 5) {
                val othersAmount = categoryTotals.drop(5).sumOf { (_, amount) -> amount }
                val othersPercentage = (othersAmount / totalAmount * 100).toInt()

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray.copy(alpha = 0.3f)
                )

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category name
                        Text(
                            text = "Others",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Amount and percentage
                        Row {
                            Text(
                                text = "${String.format("%.2f", othersAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$othersPercentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Color.LightGray.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(othersPercentage / 100f)
                                .background(Color.Gray, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}