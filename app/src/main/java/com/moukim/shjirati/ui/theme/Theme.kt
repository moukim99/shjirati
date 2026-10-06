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

val AppBackground = Color(0xFFFAF8FC)
val AppCard = Color(0xFFECE6F0)
val AppCardInner = Color(0xFFF7F2FA)
val AppPrimaryBrown = Color(0xFF855823)
val AppPrimaryHover = Color(0xFF734B1D)
val AppFabBg = Color(0xFFFCE7C8)
val AppFabText = Color(0xFF322514)
val AppTextMain = Color(0xFF1D1A22)
val AppTextMuted = Color(0xFF49454E)
val AppWaterDrop = Color(0xFF433D4F)

val PlantLavender = Color(0xFFE9E4F5)
val PlantLavenderActive = Color(0xFFE9E2F8)
val PlantLavenderBorder = Color(0xFFDDD5ED)
val PlantAccent = Color(0xFF8E7CC3)
val PlantDark = Color(0xFF2D2938)
val PlantMuted = Color(0xFF7A7587)
val PlantWarmAmber = Color(0xFFC97D46)
val PlantBorderLight = Color(0xFFECE9F3)

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
