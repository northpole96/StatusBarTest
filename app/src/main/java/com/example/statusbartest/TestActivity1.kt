import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.statusbartest.ui.theme.StatusBarTestTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    var isExpense by remember { mutableStateOf(true) }
    val boxHeight by animateDpAsState(
        targetValue = if (isExpense) 60.dp else 400.dp,
        animationSpec = tween(300)
    )
    val boxWidth by animateDpAsState(
        targetValue = if (isExpense) 60.dp else 1000.dp, // Using a large value that will be constrained by parent
        animationSpec = tween(500)
    )

    StatusBarTestTheme {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start // Align to start to keep the same origin point
                ) {
                   M3DatePickerSample()


                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3DatePickerSample() {
    // Define custom black and white colors
    val blackAndWhiteColors = darkColorScheme().copy(
        primary = androidx.compose.ui.graphics.Color.Black,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        surface = androidx.compose.ui.graphics.Color.White,
        onSurface = androidx.compose.ui.graphics.Color.Black,
        background = androidx.compose.ui.graphics.Color.White,
        onBackground = androidx.compose.ui.graphics.Color.Black,
        secondaryContainer = androidx.compose.ui.graphics.Color.White,
        onSecondaryContainer = androidx.compose.ui.graphics.Color.Black,
        tertiaryContainer = androidx.compose.ui.graphics.Color.White,
        onTertiaryContainer = androidx.compose.ui.graphics.Color.Black,
    )

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    MaterialTheme(colorScheme = blackAndWhiteColors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selected Date: ${selectedDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.Black
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Black)
            ) {
                Text("Select Date")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )

        MaterialTheme(colorScheme = blackAndWhiteColors) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                colors = DatePickerDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black,
                    headlineContentColor = androidx.compose.ui.graphics.Color.Black,
                    weekdayContentColor = androidx.compose.ui.graphics.Color.Black,
                    subheadContentColor = androidx.compose.ui.graphics.Color.Black,
                    yearContentColor = androidx.compose.ui.graphics.Color.Black,
                    currentYearContentColor = androidx.compose.ui.graphics.Color.Black,
                    selectedYearContentColor = androidx.compose.ui.graphics.Color.White,
                    selectedYearContainerColor = androidx.compose.ui.graphics.Color.Black,
                    dayContentColor = androidx.compose.ui.graphics.Color.Black,
                    selectedDayContentColor = androidx.compose.ui.graphics.Color.White,
                    selectedDayContainerColor = androidx.compose.ui.graphics.Color.Black,
                    todayContentColor = androidx.compose.ui.graphics.Color.Black,
                    todayDateBorderColor = androidx.compose.ui.graphics.Color.Black,
                ),
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // Convert milliseconds to LocalDate
                                val days = millis / (24 * 60 * 60 * 1000)
                                selectedDate = LocalDate.ofEpochDay(days)
                            }
                            showDatePicker = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        )
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(16.dp),
                    colors = DatePickerDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = androidx.compose.ui.graphics.Color.Black,
                        headlineContentColor = androidx.compose.ui.graphics.Color.Black,
                        weekdayContentColor = androidx.compose.ui.graphics.Color.Black,
                        subheadContentColor = androidx.compose.ui.graphics.Color.Black,
                        yearContentColor = androidx.compose.ui.graphics.Color.Black,
                        currentYearContentColor = androidx.compose.ui.graphics.Color.Black,
                        selectedYearContentColor = androidx.compose.ui.graphics.Color.White,
                        selectedYearContainerColor = androidx.compose.ui.graphics.Color.Black,
                        dayContentColor = androidx.compose.ui.graphics.Color.Black,
                        selectedDayContentColor = androidx.compose.ui.graphics.Color.White,
                        selectedDayContainerColor = androidx.compose.ui.graphics.Color.Black,
                        todayContentColor = androidx.compose.ui.graphics.Color.Black,
                        todayDateBorderColor = androidx.compose.ui.graphics.Color.Black,
                    )
                )
            }
        }
    }
}