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
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun AnimationPreview(){
    StatusBarTestTheme {
        Column (
            Modifier.fillMaxSize().padding(12.dp)
        ){
//            CoordinatedTransitionDemo()
//            CrossfadeElementsDemo()
            Spacer(Modifier.weight(1f))
//        AnimatedTabLayout()
        CrossfadeTabLayout()
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



@Composable
fun CoordinatedTransitionDemo() {
    // Shared state to trigger transitions
    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isExpanded, label = "coordinatedTransition")

    // Define animations for each box with staggered delays
    val box1Size by transition.animateDp(
        transitionSpec = { tween(durationMillis = 400) },
        label = "box1Size"
    ) { if (it) 150.dp else 50.dp }

    val box2Size by transition.animateDp(
        transitionSpec = { tween(durationMillis = 400, delayMillis = 200) }, // Delayed start
        label = "box2Size"
    ) { if (it) 150.dp else 50.dp }

    val box3Offset by transition.animateDp(
        transitionSpec = { tween(durationMillis = 400, delayMillis = 400) }, // Further delay
        label = "box3Offset"
    ) { if (it) 100.dp else 0.dp }

    val box1Color by transition.animateColor(
        transitionSpec = { tween(durationMillis = 400) },
        label = "box1Color"
    ) { if (it) Color.Blue else Color.Gray }

    val box2Color by transition.animateColor(
        transitionSpec = { tween(durationMillis = 400, delayMillis = 200) },
        label = "box2Color"
    ) { if (it) Color.Green else Color.Gray }

    // Layout with coordinated elements
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box 1: Size and color
        Box(
            modifier = Modifier
                .size(box1Size)
                .background(box1Color)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Box 2: Size and color (delayed)
        Box(
            modifier = Modifier
                .size(box2Size)
                .background(box2Color)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Box 3: Offset (further delayed)
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(y = box3Offset)
                .background(Color.Red)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Trigger button
        Button(onClick = { isExpanded = !isExpanded }) {
            Text(text = if (isExpanded) "Collapse" else "Expand")
        }
    }
}


@Composable
fun CrossfadeElementsDemo() {
    var showImage by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = showImage,
            animationSpec = tween(durationMillis = 700),
            label = "elementCrossfade"
        ) { isImageVisible ->
            if (isImageVisible) {
                // First element: An image placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Cyan),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image Placeholder", color = Color.Black)
                }
            } else {
                // Second element: A text block
                Column(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Yellow),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Hello, Compose!", style = MaterialTheme.typography.headlineLarge)
                    Text("This is a crossfade demo.")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showImage = !showImage }) {
            Text(text = if (showImage) "Show Text" else "Show Image")
        }
    }
}


@Composable
fun AnimatedTabLayout() {
    // State to track the currently selected tab
    var selectedTab by remember { mutableStateOf("tiger") }

    // List of available tabs
    val tabs = listOf("tiger", "lion", "horse")

    // Tab container
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(Color.White, RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            // Animate background color based on selection
            val backgroundColor by animateColorAsState(
                targetValue = if (selectedTab == tab) Color.Gray else Color.White,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "tab_background"
            )

            // Tab title container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { selectedTab = tab },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.capitalize(),
                    color = if (selectedTab == tab) Color.White else Color.Black,
                    fontSize = 16.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CrossfadeTabLayout() {
    // State to track the currently selected tab
    var selectedTab by remember { mutableStateOf("tiger") }

    // List of available tabs
    val tabs = listOf("tiger", "lion", "horse")

    // Tab container
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(
                        targetState = selectedTab == tab,
                        animationSpec = tween(durationMillis = 300),
                        label = "tab_fade"
                    ) { isSelected ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isSelected) Color.Gray else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.capitalize(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Optional: Content area based on selected tab
        Spacer(modifier = Modifier.height(16.dp))

        // Example of showing content based on selected tab
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 300),
            label = "content_fade"
        ) { tab ->
            Text(
                text = "Content for $tab",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}