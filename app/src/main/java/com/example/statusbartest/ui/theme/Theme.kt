package com.example.statusbartest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define minimalist colors
private val MinimalLightColorScheme = lightColorScheme(
    primary = Color.Black,          // Black for primary elements
    onPrimary = Color.White,        // White text/icons on primary
    primaryContainer = Color.Gray,  // Example: Use gray or map to Black/White
    onPrimaryContainer = Color.Black,// Example
    secondary = Color.DarkGray,     // Example
    onSecondary = Color.White,      // Example
    secondaryContainer = Color.LightGray, // Example
    onSecondaryContainer = Color.Black, // Example
    tertiary = Color.DarkGray,      // Example
    onTertiary = Color.White,       // Example
    tertiaryContainer = Color.LightGray, // Example
    onTertiaryContainer = Color.Black,  // Example
    background = Color.White,       // White background
    onBackground = Color.Black,     // Black text on white background
    surface = Color.White,          // White surfaces (cards, dialogs, etc.)
    onSurface = Color.Black,        // Black text/icons on surfaces
    surfaceVariant = Color.LightGray, // Example: Use a light gray for variants
    onSurfaceVariant = Color.Black, // Example
    outline = Color.Gray,           // Example: Gray outline
    inverseOnSurface = Color.White, // Example
    inverseSurface = Color.Black,   // Example
    inversePrimary = Color.White,   // Example
    surfaceTint = Color.Transparent, // *** Explicitly set tint if you don't want it ***
    outlineVariant = Color.LightGray,// Example
    scrim = Color.Black,            // Example

    error = Color.Red,              // Red for delete, errors, warnings
    onError = Color.White,          // White text/icons on red
    errorContainer = Color(0xFFFFDAD6), // Example: Light red for error containers
    onErrorContainer = Color(0xFF410002) // Example: Dark red text on error containers
)

private val MinimalDarkColorScheme = darkColorScheme(
    primary = Color.White,          // White for primary elements
    onPrimary = Color.Black,        // Black text/icons on primary
    primaryContainer = Color.DarkGray, // Example
    onPrimaryContainer = Color.White, // Example
    secondary = Color.LightGray,    // Example
    onSecondary = Color.Black,      // Example
    secondaryContainer = Color.Gray,  // Example
    onSecondaryContainer = Color.White, // Example
    tertiary = Color.LightGray,     // Example
    onTertiary = Color.Black,       // Example
    tertiaryContainer = Color.Gray,   // Example
    onTertiaryContainer = Color.White, // Example
    background = Color.Black,       // Black background
    onBackground = Color.White,     // White text on black background
    surface = Color.Black,          // Black surfaces
    onSurface = Color.White,        // White text/icons on surfaces
    surfaceVariant = Color.DarkGray,  // Example: Use a dark gray for variants
    onSurfaceVariant = Color.White, // Example
    outline = Color.Gray,           // Example: Gray outline
    inverseOnSurface = Color.Black, // Example
    inverseSurface = Color.White,   // Example
    inversePrimary = Color.Black,   // Example
    surfaceTint = Color.Transparent, // *** Explicitly set tint if you don't want it ***
    outlineVariant = Color.DarkGray, // Example
    scrim = Color.Black,           // Example

    error = Color.Red,              // Red for delete, errors, warnings
    onError = Color.Black,          // Black text/icons on red
    errorContainer = Color(0xFF93000A), // Example: Dark red for error containers
    onErrorContainer = Color(0xFFFFDAD6) // Example: Light red text on error containers
)

@Composable
fun StatusBarTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    val colorScheme = if (darkTheme) MinimalDarkColorScheme else MinimalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme.copy(surfaceTint = Color.Transparent),
        typography = Typography,
        content = content
    )
}