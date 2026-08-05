package com.phytotec.recepcion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Matches phytotec_v2's default brand color (see BRAND_PRIMARY_COLOR in the
// web app's .env) so the mobile app looks like part of the same product.
private val PhytotecGreen = Color(0xFF059669)
private val PhytotecGreenDark = Color(0xFF047857)

private val LightColors = lightColorScheme(
    primary = PhytotecGreen,
    onPrimary = Color.White,
    secondary = PhytotecGreenDark,
)

private val DarkColors = darkColorScheme(
    primary = PhytotecGreen,
    onPrimary = Color.White,
    secondary = PhytotecGreenDark,
)

@Composable
fun RecepcionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
