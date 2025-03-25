package com.example.statusbartest

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchScreen() {
    var count by remember { mutableStateOf(0) }
    Column (modifier= Modifier.fillMaxSize().background(Color.White)){
        Text("Hello Lambda")
        Text("greenscreen")
        Text("Putelias")
Button(onClick = { count+=1 }) {
    Text("Plus")
}
        Text("$count")
        Button(onClick = { count-=1 }) {
    Text("Minus")
}
        SimpleMenu()
    }
}


@Composable
fun SimpleMenu() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {

        TextButton(onClick = { expanded = true }) {
            Text("Open Menu")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Option 1", "Option 2", "Option 3", "Option 4").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        Toast.makeText(context, "$option clicked", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun SearchScreenPreview() {
    SearchScreen()
}