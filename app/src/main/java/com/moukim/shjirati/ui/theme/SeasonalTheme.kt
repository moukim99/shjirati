package com.moukim.shjirati.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.moukim.shjirati.domain.Season
import com.moukim.shjirati.domain.currentSeason

private fun schemeFor(season: Season): ColorScheme = when (season) {
    Season.SPRING -> lightColorScheme()
    Season.SUMMER -> lightColorScheme()
    Season.AUTUMN -> lightColorScheme()
    Season.WINTER -> lightColorScheme()
}

@Composable
fun ShjiratiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = schemeFor(currentSeason()),
        content = content
    )
}
