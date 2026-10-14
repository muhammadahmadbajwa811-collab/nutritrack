package com.nutritrack.app.designsystem.theme

import androidx.compose.ui.graphics.Color

// Light Theme Tokens (PRD Section 5.2)
val PrimaryLight = Color(0xFFD62839)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFCEBED)
val OnPrimaryContainerLight = Color(0xFF7A0F1B)
val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFFAF6F6)
val OutlineLight = Color(0xFFEADFE0)
val OnSurfaceLight = Color(0xFF1E1819)
val OnSurfaceVariantLight = Color(0xFF6B6163)

// Dark Theme Tokens (PRD Section 5.2)
val PrimaryDark = Color(0xFFFF6B75)
val OnPrimaryDark = Color(0xFF3A0A0F)
val PrimaryContainerDark = Color(0xFF3A1519)
val OnPrimaryContainerDark = Color(0xFFFFD9DC)
val BackgroundDark = Color(0xFF131011)
val SurfaceDark = Color(0xFF1B1617)
val SurfaceVariantDark = Color(0xFF241E1F)
val OutlineDark = Color(0xFF3A3133)
val OnSurfaceDark = Color(0xFFF6F0F0)
val OnSurfaceVariantDark = Color(0xFFA99FA1)

// Semantic & Functional Tokens (PRD Section 5.2)
val SuccessLight = Color(0xFF1F8A70)
val SuccessDark = Color(0xFF3CC4A0)
val WarningLight = Color(0xFFE0861A)
val WarningDark = Color(0xFFF2A14A)
val InfoLight = Color(0xFF2F6FDE)
val InfoDark = Color(0xFF6C9BFF)

// Macros
val MacroProteinLight = Color(0xFF2F6FDE)
val MacroProteinDark = Color(0xFF6C9BFF)
val MacroCarbsLight = Color(0xFFE0861A)
val MacroCarbsDark = Color(0xFFF2A14A)
val MacroFatLight = Color(0xFF1F8A70)
val MacroFatDark = Color(0xFF3CC4A0)
val WaterTrackerLight = Color(0xFF2F9BE0)
val WaterTrackerDark = Color(0xFF6CBBF2)

data class NutriTrackColors(
    val protein: Color,
    val carbs: Color,
    val fat: Color,
    val water: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val brandRed: Color
)

val LocalNutriTrackColors = androidx.compose.runtime.staticCompositionLocalOf {
    NutriTrackColors(
        protein = MacroProteinLight,
        carbs = MacroCarbsLight,
        fat = MacroFatLight,
        water = WaterTrackerLight,
        success = SuccessLight,
        warning = WarningLight,
        info = InfoLight,
        brandRed = PrimaryLight
    )
}

val NutriTrackColorsLight = NutriTrackColors(
    protein = MacroProteinLight,
    carbs = MacroCarbsLight,
    fat = MacroFatLight,
    water = WaterTrackerLight,
    success = SuccessLight,
    warning = WarningLight,
    info = InfoLight,
    brandRed = PrimaryLight
)

val NutriTrackColorsDark = NutriTrackColors(
    protein = MacroProteinDark,
    carbs = MacroCarbsDark,
    fat = MacroFatDark,
    water = WaterTrackerDark,
    success = SuccessDark,
    warning = WarningDark,
    info = InfoDark,
    brandRed = PrimaryDark
)

val androidx.compose.material3.MaterialTheme.nutriTrackColors: NutriTrackColors
    @androidx.compose.runtime.Composable
    get() = LocalNutriTrackColors.current

