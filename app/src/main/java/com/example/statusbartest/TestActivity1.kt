package com.example.statusbartest

import android.os.Bundle
import android.widget.Button
import androidx.compose.foundation.layout.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.statusbartest.ui.theme.StatusBarTestTheme

class TestActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatusBarTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting2(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview2() {
//    StatusBarTestTheme {
//        Column(Modifier
//            .fillMaxSize()
//            .padding(12.dp)) {
//            TransactionTypeSelectionBar()
//            Spacer(modifier = Modifier.weight(1f))
//            AmountBlock()
//            KeyboardTopBlock()
//            Spacer(modifier=Modifier.height(12.dp))
//
//            CustomDigitKeyboard2()
//        }
//
//    }
//}

@Composable
fun DigitKeyboard() {

}

@Composable
fun CustomDigitKeyboard2() {
    val colorButton = Color.LightGray.copy(alpha = 0.2f)
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "Del"),
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
                    if (label != "Del") {
                        Button(
                            onClick = {

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
                            onClick = { /* Handle click */ },
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




@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    StatusBarTestTheme {
        Column(
            Modifier
                .fillMaxSize() // The parent Column fills the entire screen
                .padding(12.dp)
        ) {
            TransactionTypeSelectionBar()
            Spacer(modifier = Modifier.weight(1f))
           AmountBlock()
            Spacer(modifier = Modifier.weight(1f))
            KeyboardTopBlock()
            Spacer(modifier = Modifier.height(12.dp))
            CustomDigitKeyboard2()
        }
    }
}

@Composable
fun AmountBlock() {
    Column ( // Example visual for the AmountBlock
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Yellow.copy(alpha = 0.3f)) // Just for visualization
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("0", fontSize = 120.sp)
    }
}

@Composable
fun TransactionTypeSelectionBar(){
    // Your implementation
    Text("Transaction Type Selection", modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun KeyboardTopBlock() {
    // Your implementation
    val colorBorder= Color.Black.copy(alpha = 0.2f)
    val borderWidth=1.5.dp
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier
                .padding(end = 12.dp)
                .weight(1f)
                .border(borderWidth, colorBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = "Calendar",
                tint = Color.Black
            )

            Text("Today,27 Mar", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("10:13", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Row(
            modifier = Modifier
                .border(borderWidth, colorBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Contrast,
                contentDescription = "Category",
                tint = Color.Black
            )
            Spacer(Modifier.width(6.dp))
            Text("Category", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}