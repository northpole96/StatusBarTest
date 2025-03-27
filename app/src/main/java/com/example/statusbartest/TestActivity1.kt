package com.example.statusbartest

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    StatusBarTestTheme {
        Column (Modifier.fillMaxSize().padding(12.dp)){
            Spacer(modifier=Modifier.weight(1f))
            CustomDigitKeyboard2()
        }

    }
}

@Composable
fun DigitKeyboard() {

}

@Composable
fun CustomDigitKeyboard2() {
    val colorButton= Color.LightGray.copy(alpha = 0.2f)
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "Del"),
//        listOf("Clear")
    )

    Column {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    if(label != "Del"){
                    Button(
                        onClick = {

                        },colors= ButtonDefaults.buttonColors(
                            containerColor = colorButton,
                            contentColor = Color.White
                        ) ,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).weight(1f).height(60.dp)
                    ) {
                        Text(label,fontSize = MaterialTheme.typography.headlineLarge.fontSize, color = Color.Black)
                    }
                    }
                    else{

                        IconButton(
                            onClick = { /* Handle click */ },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).weight(1f).height(60.dp).background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp)))
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

