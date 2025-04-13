package com.example.statusbartest

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform // Import needed for transitionSpec
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
import androidx.compose.foundation.layout.PaddingValues // Import needed for LazyColumn
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn
import androidx.compose.foundation.lazy.items // Import items extension function
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
import androidx.compose.material3.LinearProgressIndicator // Import LinearProgressIndicator
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.NumberFormat // Import NumberFormat
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

// --- Enums ---
enum class InsightTimeFrame {
    WEEK, MONTH, YEAR
}

enum class TransactionViewMode {
    INCOME, EXPENSE, BOTH // Changed default to BOTH
}

// --- Data class for category breakdown ---
data class CategorySpending(
    val category: Category, // Use the Category data class
    val amount: Double,
    val percentage: Float
)

data class ChartDataPoint(
    val xLabel: String, // Label for the X-axis point (e.g., "Mon", "15", "Mar")
    val yValue: Double  // Value for the Y-axis
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
) {
    // --- State Management ---
    var timeFrame by remember { mutableStateOf(InsightTimeFrame.MONTH) }
    var viewMode by remember { mutableStateOf(TransactionViewMode.EXPENSE) } // Default to Expense view
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    var dragOffset by remember { mutableStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // --- Date Calculations ---
    val periodStart = remember(currentDate, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            InsightTimeFrame.MONTH -> currentDate.withDayOfMonth(1)
            InsightTimeFrame.YEAR -> currentDate.withDayOfYear(1) // Corrected for Year start
        }
    }

    val periodEnd = remember(periodStart, timeFrame) {
        when (timeFrame) {
            InsightTimeFrame.WEEK -> periodStart.plusDays(6)
            InsightTimeFrame.MONTH -> periodStart.plusMonths(1).minusDays(1)
            InsightTimeFrame.YEAR -> periodStart.plusYears(1).minusDays(1)
        }
    }

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

    // --- Transaction Filtering ---
    val currentPeriodTransactions = remember(allTransactions, periodStart, periodEnd) {
        allTransactions.filter { transaction ->
            try {
                val transactionDate = LocalDate.parse(transaction.date)
                !transactionDate.isBefore(periodStart) && !transactionDate.isAfter(periodEnd)
            } catch (e: Exception) {
                false // Handle potential parsing errors
            }
        }
    }

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

    // --- Income/Expense Calculations ---
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

    // --- Percentage Change Calculations ---
    val incomeChange = remember(currentIncome, previousIncome) {
        calculatePercentageChange(currentIncome, previousIncome)
    }
    val expenseChange = remember(currentExpense, previousExpense) {
        calculatePercentageChange(currentExpense, previousExpense)
    }

    // --- Category Breakdown Calculation ---
    // Collect category flows ONCE outside the remember block for efficiency
    val expenseDefaultCategories by categoryViewModel.expenseDefaultCategories.collectAsState(
        initial = emptyList()
    )
    val expenseCustomCategories by categoryViewModel.expenseCustomCategories.collectAsState(initial = emptyList())
    val incomeDefaultCategories by categoryViewModel.incomeDefaultCategories.collectAsState(initial = emptyList())
    val incomeCustomCategories by categoryViewModel.incomeCustomCategories.collectAsState(initial = emptyList())

    val categoryBreakdownData = remember(
        currentPeriodTransactions,
        viewMode,
        expenseDefaultCategories, // Depend on collected state
        expenseCustomCategories,  // Depend on collected state
        incomeDefaultCategories,  // Depend on collected state
        incomeCustomCategories    // Depend on collected state
    ) {
        // Combine the relevant collected category lists based on viewMode
        val allRelevantCategories = when (viewMode) {
            TransactionViewMode.INCOME -> incomeDefaultCategories + incomeCustomCategories
            TransactionViewMode.EXPENSE -> expenseDefaultCategories + expenseCustomCategories
            TransactionViewMode.BOTH -> expenseDefaultCategories + expenseCustomCategories + incomeDefaultCategories + incomeCustomCategories // Combine all for BOTH
        }

        // Filter transactions based on view mode
        val transactionsToAnalyze = when (viewMode) {
            TransactionViewMode.INCOME -> currentPeriodTransactions.filter { it.type == "Income" }
            TransactionViewMode.EXPENSE -> currentPeriodTransactions.filter { it.type == "Expense" }
            TransactionViewMode.BOTH -> currentPeriodTransactions // Analyze all transactions for BOTH
        }

        val totalAmount = transactionsToAnalyze.sumOf { it.amount }
        if (totalAmount == 0.0) return@remember emptyList<CategorySpending>() // Avoid division by zero

        transactionsToAnalyze
            .groupBy { it.category }
            .mapNotNull { (categoryName, transactions) ->
                val categoryAmount = transactions.sumOf { it.amount }
                // Find the Category object from the combined list
                val category = allRelevantCategories.find { it.name == categoryName }
                category?.let { cat -> // Only include if category object is found
                    CategorySpending(
                        category = cat,
                        amount = categoryAmount,
                        percentage = (categoryAmount / totalAmount * 100).toFloat()
                    )
                }
            }
            .sortedByDescending { it.amount } // Sort by amount
    }
// --- Chart Data Preparation ---
    val chartData = remember(currentPeriodTransactions, timeFrame, viewMode) {
        prepareChartData(currentPeriodTransactions, timeFrame, viewMode, periodStart, periodEnd)
    }

    // --- Formatting ---
    val periodTitle = remember(periodStart, timeFrame) {
        formatPeriodTitle(periodStart, timeFrame)
    }

    // --- Navigation Functions ---
    val navigateToPreviousPeriod = {
        if (!isAnimating) {
            isAnimating = true
            dragOffset = 300f // Simulate drag for animation effect
            coroutineScope.launch {
                currentDate = when (timeFrame) {
                    InsightTimeFrame.WEEK -> currentDate.minusWeeks(1)
                    InsightTimeFrame.MONTH -> currentDate.minusMonths(1)
                    InsightTimeFrame.YEAR -> currentDate.minusYears(1)
                }
                dragOffset = 0f // Reset offset after navigation
                isAnimating = false
            }
        }
    }

    val navigateToNextPeriod = {
        if (!isAnimating) {
            val today = LocalDate.now()
            val nextPeriodStart = when (timeFrame) {
                InsightTimeFrame.WEEK -> periodStart.plusWeeks(1)
                InsightTimeFrame.MONTH -> periodStart.plusMonths(1)
                InsightTimeFrame.YEAR -> periodStart.plusYears(1)
            }
            // Prevent navigating beyond the current period
            if (!nextPeriodStart.isAfter(today)) {
                isAnimating = true
                dragOffset = -300f // Simulate drag for animation effect
                coroutineScope.launch {
                    currentDate = when (timeFrame) {
                        InsightTimeFrame.WEEK -> currentDate.plusWeeks(1)
                        InsightTimeFrame.MONTH -> currentDate.plusMonths(1)
                        InsightTimeFrame.YEAR -> currentDate.plusYears(1)
                    }
                    dragOffset = 0f // Reset offset after navigation
                    isAnimating = false
                }
            }
        }
    }


    // --- UI Layout ---
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text("Insights") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface), // Use theme color
                actions = {
                    InsightsTimePeriodDropdown(
                        timeFrame = timeFrame,
                        onTimeFrameSelected = {
                            timeFrame = it
                            // Maybe reset currentDate to now when timeframe changes?
                            // currentDate = LocalDate.now()
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Use theme background
                    .padding(paddingValues)
                // Remove verticalScroll here, apply to specific sections if needed
            ) {
                // --- Period Navigation ---
                PeriodNavigation(
                    periodTitle = periodTitle,
                    onPrevious = navigateToPreviousPeriod,
                    onNext = navigateToNextPeriod,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // --- Animated Content for Swipe ---
                AnimatedContent(
                    targetState = currentDate, // Animate when currentDate changes
                    transitionSpec = {
                        // Determine slide direction based on drag or button click
                        val direction = if (dragOffset > 0 || targetState.isBefore(initialState)) {
                            // Sliding in from Left
                            slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                        } else {
                            // Sliding in from Right
                            slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                        }
                        direction.using(
                            // Disable fade-in/out during slide
                            SizeTransform(clip = false)
                        )
                    },
                    label = "insightsContentAnimation"
                ) {  // Use _ to mark targetDate as unused
                    // --- Main Content Area ---
                    // Use LazyColumn for scrollable content instead of Column + verticalScroll
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragOffset = 0f },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                    },
                                    onDragEnd = {
                                        when {
                                            dragOffset > 150 -> navigateToPreviousPeriod() // Swipe Right
                                            dragOffset < -150 -> navigateToNextPeriod() // Swipe Left
                                        }
                                        dragOffset = 0f // Reset drag offset
                                    }
                                )
                            },
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- Summary Card ---
                        item {
                            InsightSummaryCard(
                                currentIncome = currentIncome,
                                currentExpense = currentExpense,
                                incomeChange = incomeChange,
                                expenseChange = expenseChange
                            )
                        }
                        // --- Spending Trend Chart Card ---
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = when (viewMode) { // Dynamic title based on view mode
                                            TransactionViewMode.INCOME -> "Income Trend"
                                            TransactionViewMode.EXPENSE -> "Expense Trend"
                                            TransactionViewMode.BOTH -> "Net Trend"
                                        } + " (${
                                            timeFrame.name.lowercase().capitalize(Locale.ROOT)
                                        })",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // --- Line Chart Composable ---
                                    if (chartData.isNotEmpty()) {
                                        SpendingTrendChart(
                                            dataPoints = chartData,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp), // Fixed height for the chart
                                            lineColor = when (viewMode) { // Color based on view mode
                                                TransactionViewMode.INCOME -> Color(0xFF4CAF50) // Green
                                                TransactionViewMode.EXPENSE -> Color(0xFFF44336) // Red
                                                TransactionViewMode.BOTH -> MaterialTheme.colorScheme.primary // Theme primary
                                            }
                                        )
                                    } else {
                                        // Show message if no data for the chart
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No data for chart in this period.")
                                        }
                                    }
                                }
                            }
                        } // End of Chart Card item

                        // --- Category Breakdown ---
                        item {
                            CategoryBreakdownCard(
                                categoryData = categoryBreakdownData, // Pass processed data
                                viewMode = viewMode,
                                onViewModeChange = { viewMode = it },
                                totalAmount = when (viewMode) { // Pass the correct total
                                    TransactionViewMode.INCOME -> currentIncome
                                    TransactionViewMode.EXPENSE -> currentExpense
                                    TransactionViewMode.BOTH -> currentIncome - currentExpense // Or adjust based on BOTH logic
                                }
                            )
                        }

                        // --- Add more insights here ---
                        // Example: Spending Trend Chart (Placeholder)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Spending Trend (${
                                            timeFrame.name.lowercase().capitalize(Locale.ROOT)
                                        })",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // TODO: Implement a line chart or bar chart showing spending over the period
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.LightGray.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Chart Placeholder")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// --- Helper Functions ---
fun prepareChartData(
    transactions: List<Transaction>,
    timeFrame: InsightTimeFrame,
    viewMode: TransactionViewMode,
    periodStart: LocalDate,
    periodEnd: LocalDate
): List<ChartDataPoint> {

    // Determine which value to plot based on view mode
    val valueSelector: (Transaction) -> Double = when (viewMode) {
        TransactionViewMode.INCOME -> { t -> if (t.type == "Income") t.amount else 0.0 }
        TransactionViewMode.EXPENSE -> { t -> if (t.type == "Expense") t.amount else 0.0 }
        TransactionViewMode.BOTH -> { t -> if (t.type == "Income") t.amount else -t.amount } // Net value
    }

    // Group and sum transactions based on the time frame
    return when (timeFrame) {
        InsightTimeFrame.WEEK -> {
            // Group by DayOfWeek (Mon=1, Sun=7)
            val grouped = transactions.groupBy {
                LocalDate.parse(it.date).dayOfWeek
            }
            // Ensure all days of the week are present, even with 0 value
            DayOfWeek.entries.map { day ->
                val total = grouped[day]?.sumOf(valueSelector) ?: 0.0
                ChartDataPoint(
                    xLabel = day.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.getDefault()
                    ), // "Mon", "Tue"
                    yValue = total
                )
            }
        }

        InsightTimeFrame.MONTH -> {
            // Group by day of the month
            val grouped = transactions.groupBy {
                LocalDate.parse(it.date).dayOfMonth
            }
            // Create points for each day in the month
            val daysInMonth = periodStart.lengthOfMonth()
            (1..daysInMonth).map { day ->
                val total = grouped[day]?.sumOf(valueSelector) ?: 0.0
                ChartDataPoint(xLabel = day.toString(), yValue = total)
            }
        }

        InsightTimeFrame.YEAR -> {
            // Group by month
            val grouped = transactions.groupBy {
                LocalDate.parse(it.date).month
            }
            // Ensure all months are present
            Month.entries.map { month ->
                val total = grouped[month]?.sumOf(valueSelector) ?: 0.0
                ChartDataPoint(
                    xLabel = month.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.getDefault()
                    ), // "Jan", "Feb"
                    yValue = total
                )
            }
        }
    }
}


// --- Spending Trend Chart Composable ---
@Composable
fun SpendingTrendChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    labelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No chart data available.")
        }
        return
    }

    val density = LocalDensity.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = labelColor.hashCode() // Use labelColor
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = density.run { 10.sp.toPx() } // Smaller text size for labels
        }
    }

    // Calculate Y-axis range (add some padding)
    val minYValue = dataPoints.minOfOrNull { it.yValue } ?: 0.0
    val maxYValue = dataPoints.maxOfOrNull { it.yValue } ?: 0.0
    // Adjust padding based on whether min/max are zero or very close
    val yPadding =
        if (maxYValue == minYValue) 10.0 else (maxYValue - minYValue) * 0.1 // 10% padding
    val finalMinY =
        if (minYValue == 0.0 && maxYValue == 0.0) -10.0 else (minYValue - yPadding) // Ensure range if all zero
    val finalMaxY = if (minYValue == 0.0 && maxYValue == 0.0) 10.0 else (maxYValue + yPadding)
    val yRange = finalMaxY - finalMinY

    Canvas(modifier = modifier) {
        val chartHeight =
            size.height - density.run { 20.dp.toPx() } // Reserve space for labels bottom
        val chartWidth = size.width - density.run { 30.dp.toPx() } // Reserve space for labels left
        val xOffset = density.run { 30.dp.toPx() } // Left padding for Y labels
        val yOffset = density.run { 10.dp.toPx() } // Top padding

        // --- Draw Grid Lines & Y-Axis Labels ---
        val gridLineCount = 4 // Number of horizontal grid lines
        (0..gridLineCount).forEach { i ->
            val y = yOffset + chartHeight * (1f - i.toFloat() / gridLineCount)
            drawLine(
                color = gridColor,
                start = Offset(xOffset, y),
                end = Offset(xOffset + chartWidth, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)) // Dashed lines
            )
            // Draw Y-axis label
            val labelValue = finalMinY + (yRange * i / gridLineCount)
            drawContext.canvas.nativeCanvas.drawText(
                NumberFormat.getNumberInstance()
                    .format(labelValue.roundToInt()), // Simple formatting
                xOffset - density.run { 4.dp.toPx() }, // Position left of axis
                y + density.run { 4.dp.toPx() }, // Adjust vertical position
                textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
            )
        }

        // --- Draw X-Axis Labels & Line Path ---
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = xOffset + chartWidth * index / max(
                1,
                dataPoints.size - 1
            ) // Avoid division by zero if only 1 point
            // Map yValue to canvas y-coordinate (inverted)
            val y = yOffset + chartHeight * (1 - ((point.yValue - finalMinY) / yRange).toFloat())

            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }

            // Draw X-axis label
            drawContext.canvas.nativeCanvas.drawText(
                point.xLabel,
                x,
                size.height - density.run { 4.dp.toPx() }, // Position below chart
                textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
            )
        }

        // Draw the line chart path
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // Optional: Draw points on the line
        dataPoints.forEachIndexed { index, point ->
            val x = xOffset + chartWidth * index / max(1, dataPoints.size - 1)
            val y = yOffset + chartHeight * (1 - ((point.yValue - finalMinY) / yRange).toFloat())
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y.toFloat())
            )
        }
    }
}

fun calculatePercentageChange(current: Double, previous: Double): Int {
    return when {
        previous > 0 -> ((current - previous) / previous * 100).roundToInt()
        current > 0 -> 100 // Infinite increase if previous was 0
        else -> 0 // No change if both are 0 or negative
    }
}

fun formatPeriodTitle(periodStart: LocalDate, timeFrame: InsightTimeFrame): String {
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy") // Include year for month
    val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d")

    return when (timeFrame) {
        InsightTimeFrame.WEEK -> {
            val weekEndDate = periodStart.plusDays(6)
            // Show year only if start and end years differ or if it's not the current year
            val currentYear = LocalDate.now().year
            val startFormat = if (periodStart.year != currentYear) "MMM d, yyyy" else "MMM d"
            val endFormat = when {
                weekEndDate.year != periodStart.year -> "MMM d, yyyy" // End year differs from start
                weekEndDate.year != currentYear -> "MMM d, yyyy" // End year is not current year
                else -> "MMM d"
            }
            "${periodStart.format(DateTimeFormatter.ofPattern(startFormat))} - ${
                weekEndDate.format(
                    DateTimeFormatter.ofPattern(endFormat)
                )
            }"
        }

        InsightTimeFrame.MONTH -> periodStart.format(monthYearFormatter)
        InsightTimeFrame.YEAR -> periodStart.format(yearFormatter)
    }
}

// --- Composable Components ---

@Composable
fun InsightsTimePeriodDropdown(
    timeFrame: InsightTimeFrame,
    onTimeFrameSelected: (InsightTimeFrame) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timeFrameOptions =
        InsightTimeFrame.entries.map { it.name.lowercase().capitalize(Locale.ROOT) }
    val selectedTimeFrame = timeFrame.name.lowercase().capitalize(Locale.ROOT)

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        TextButton(onClick = { expanded = true }) {
            Text(text = selectedTimeFrame)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Timeframe")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface) // Theme background
        ) {
            timeFrameOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onTimeFrameSelected(InsightTimeFrame.valueOf(option.uppercase()))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PeriodNavigation(
    periodTitle: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Period")
        }
        Text(
            text = periodTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Period")
        }
    }
}

@Composable
fun InsightSummaryCard(
    currentIncome: Double,
    currentExpense: Double,
    incomeChange: Int,
    expenseChange: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Use theme color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Add subtle elevation
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Period Summary", // More descriptive title
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp) // More space below title
            )

            Row(
                horizontalArrangement = Arrangement.SpaceAround, // Space out evenly
                modifier = Modifier.fillMaxWidth()
            ) {
                // Income Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) { // Center align text
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50) // Specific Green
                    )
                    Text(
                        // Format currency using Locale
                        text = NumberFormat.getCurrencyInstance(Locale.getDefault())
                            .format(currentIncome),
                        style = MaterialTheme.typography.headlineSmall, // Slightly larger text
                        fontWeight = FontWeight.Bold
                    )
                    ChangeIndicator(change = incomeChange, type = "Income") // Pass type
                }

                // Vertical Divider
                Spacer(modifier = Modifier.width(16.dp)) // Add space around divider
                Divider(
                    modifier = Modifier
                        .height(60.dp) // Adjust height as needed
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Expense Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) { // Center align text
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF44336) // Specific Red
                    )
                    Text(
                        // Format currency using Locale
                        text = NumberFormat.getCurrencyInstance(Locale.getDefault())
                            .format(currentExpense),
                        style = MaterialTheme.typography.headlineSmall, // Slightly larger text
                        fontWeight = FontWeight.Bold
                    )
                    ChangeIndicator(change = expenseChange, type = "Expense") // Pass type
                }
            }
        }
    }
}

@Composable
fun ChangeIndicator(change: Int, type: String) {
    // Determine color based on type and change value
    val color = when (type) {
        "Income" -> if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336) // Green if >= 0, Red if < 0
        "Expense" -> if (change <= 0) Color(0xFF4CAF50) else Color(0xFFF44336) // Green if <= 0 (less spending is good), Red if > 0
        else -> MaterialTheme.colorScheme.onSurfaceVariant // Default color
    }
    val symbol = if (change >= 0) "↑" else "↓"
    val text = if (change == 0) "No change" else "$symbol ${abs(change)}%"

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.padding(top = 4.dp) // Add some top padding
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBreakdownCard(
    categoryData: List<CategorySpending>,
    viewMode: TransactionViewMode,
    onViewModeChange: (TransactionViewMode) -> Unit,
    totalAmount: Double // Pass total amount for context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Use theme color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                // Conditionally show toggle only if there's data for both types?
                // Or just allow switching views regardless.
                TransactionViewModeToggle(viewMode = viewMode, onViewModeChange = onViewModeChange)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display category list or empty state
            if (categoryData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${viewMode.name.lowercase()} data for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Use Column as LazyColumn inside another LazyColumn isn't recommended without fixed height
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Limit the number of categories shown initially, e.g., top 5
                    val topCategories = categoryData.take(5)

                    topCategories.forEach { item ->
                        CategorySpendingItem(item = item)
                    }

                    // TODO: Optionally add a "View All" button if categoryData.size > 5
                }
            }
        }
    }
}

@Composable
fun CategorySpendingItem(item: CategorySpending) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    // Safely attempt to parse color, fallback to Gray
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(item.category.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Emoji, Name, Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp) // Smaller indicator
                        .background(
                            categoryColor.copy(alpha = 0.2f),
                            CircleShape
                        ), // Use category color with alpha
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.category.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f) // Allow name to take available space
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${String.format("%.1f", item.percentage)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Subdued color for percentage
                )
            }

            // Right side: Amount
            Text(
                text = currencyFormat.format(item.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress Bar
        LinearProgressIndicator(
            progress = item.percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // Thinner progress bar
                .clip(RoundedCornerShape(3.dp)), // Rounded corners
            color = categoryColor, // Use category color for progress
            trackColor = categoryColor.copy(alpha = 0.3f), // Lighter track color
            strokeCap = StrokeCap.Round // Round ends
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionViewModeToggle(
    viewMode: TransactionViewMode,
    onViewModeChange: (TransactionViewMode) -> Unit
) {
    val options = TransactionViewMode.entries.map {
        it.name.lowercase().capitalize(Locale.ROOT)
    }
    val selectedIndex = TransactionViewMode.entries.indexOf(viewMode)

    SingleChoiceSegmentedButtonRow(modifier = Modifier.height(36.dp)) { // Control height
        options.forEachIndexed { index, label ->
            SegmentedButton(
                onClick = { onViewModeChange(TransactionViewMode.entries[index]) },
                selected = index == selectedIndex,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors( // Customize colors
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface, // Use surface for inactive
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                icon = {} // Remove default icon space if not needed
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium) // Smaller text
            }
        }
    }
}

// You should already have this extension function in Category.kt or similar
/*
fun Category.displayColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this.colorHex))
    } catch (e: Exception) {
        Color.Gray // Fallback color
    }
}
*/