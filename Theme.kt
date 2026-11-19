package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class BijliSemanticColors(
    val paid: Color,
    val paidContainer: Color,
    val unpaid: Color,
    val unpaidContainer: Color,
    val partial: Color,
    val partialContainer: Color,
    val cyanAccent: Color,
    val domestic: Color,
    val commercial: Color,
    val agricultural: Color,
    val industrial: Color,
    val cardBorder: Color,
    val cardSurfaceElevated: Color
)

private val DarkBijliSemantics = BijliSemanticColors(
    paid = EmeraldPaid,
    paidContainer = EmeraldPaidContainer,
    unpaid = CoralDue,
    unpaidContainer = CoralDueContainer,
    partial = AmberVolt,
    partialContainer = AmberVoltContainer,
    cyanAccent = ElectricCyan,
    domestic = ElectricCyan,
    commercial = AmberVolt,
    agricultural = TealAgri,
    industrial = VioletIndustrial,
    cardBorder = Slate700.copy(alpha = 0.65f),
    cardSurfaceElevated = Slate800
)

private val LightBijliSemantics = BijliSemanticColors(
    paid = EmeraldPaidDark,
    paidContainer = EmeraldPaidLightContainer,
    unpaid = CoralDueDark,
    unpaidContainer = CoralDueLightContainer,
    partial = AmberVoltDark,
    partialContainer = AmberVoltLightContainer,
    cyanAccent = ElectricCyanDark,
    domestic = ElectricCyanDark,
    commercial = AmberVoltDark,
    agricultural = TealAgriDark,
    industrial = VioletIndustrialDark,
    cardBorder = Color(0xFFCBD5E1),
    cardSurfaceElevated = Color(0xFFF8FAFC)
)

val LocalBijliColors = staticCompositionLocalOf { DarkBijliSemantics }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF042133),
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = AmberVolt,
    onSecondary = Color(0xFF2E1A00),
    secondaryContainer = AmberVoltContainer,
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = EmeraldPaid,
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = EmeraldPaidContainer,
    onTertiaryContainer = Color(0xFFBBF7D0),
    error = CoralDue,
    onError = Color(0xFF450A0A),
    errorContainer = CoralDueContainer,
    onErrorContainer = Color(0xFFFECACA),
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate400,
    surfaceContainer = Slate850,
    surfaceContainerHigh = Slate800,
    outline = Slate700,
    outlineVariant = Slate800
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyanDark,
    onPrimary = Color.White,
    primaryContainer = ElectricCyanLightContainer,
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = AmberVoltDark,
    onSecondary = Color.White,
    secondaryContainer = AmberVoltLightContainer,
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = EmeraldPaidDark,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldPaidLightContainer,
    onTertiaryContainer = Color(0xFF14532D),
    error = CoralDueDark,
    onError = Color.White,
    errorContainer = CoralDueLightContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

val BijliShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun BijliManagerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semantics = if (darkTheme) DarkBijliSemantics else LightBijliSemantics

    CompositionLocalProvider(LocalBijliColors provides semantics) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BijliTypography,
            shapes = BijliShapes,
            content = content
        )
    }
}
