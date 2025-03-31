package com.example.statusbartest

import android.os.Bundle
import android.widget.Button
import androidx.compose.foundation.layout.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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


@Preview(showBackground = true)
@Composable
fun AnimationPreview(){
    StatusBarTestTheme {
        Column (
            Modifier.fillMaxSize().padding(12.dp)
        ){
//            CoordinatedTransitionDemo()
//            CrossfadeElementsDemo()
            Spacer(Modifier.weight(1f))
//        AnimatedTabLayout()
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
//            CustomDigitKeyboard2()
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
fun TransactionTypeSelectionBar() {
    Row(Modifier.fillMaxWidth(),Arrangement.Center) {
        val interactionSource = remember { MutableInteractionSource() } // Prevent ripple

        var isExpense by remember { mutableStateOf(true) }
        Row(
            Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                .padding(4.dp)
        ) {
            Text(
                "Expense",
                Modifier.background(
                    if (isExpense) Color.LightGray.copy(alpha = 0.4f) else Color.White,
                    RoundedCornerShape(100.dp)
                ).padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable(interactionSource, indication = null) { isExpense = true },
                fontWeight = FontWeight.Medium,
                color = if (isExpense) Color.Black else Color.Gray
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Income",
                Modifier.background(
                    if (isExpense) Color.White else Color.LightGray.copy(alpha = 0.4f),
                    RoundedCornerShape(100.dp)
                ).padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable(interactionSource, indication = null) { isExpense = false },
                fontWeight = FontWeight.Medium,
                color = if (isExpense) Color.Gray else Color.Black
            )
        }
    }
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



