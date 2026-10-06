package com.moukim.shjirati.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.moukim.shjirati.domain.Season
import com.moukim.shjirati.domain.currentSeason

private fun schemeFor(season: Season): ColorScheme = lightColorScheme(
    primary = AppPrimaryBrown,
    onPrimary = Color.White,
    primaryContainer = AppFabBg,
    onPrimaryContainer = AppFabText,
    surface = AppBackground,
    onSurface = AppTextMain,
    surfaceVariant = AppCard,
    onSurfaceVariant = AppTextMuted,
    background = AppBackground,
    onBackground = AppTextMain
)

@Composable
fun ShjiratiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = schemeFor(currentSeason()),
            content = content
        )
    }
}
