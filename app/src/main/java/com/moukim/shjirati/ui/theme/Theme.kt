package com.moukim.shjirati.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.moukim.shjirati.domain.Season
import com.moukim.shjirati.domain.currentSeason

private fun schemeFor(season: Season): ColorScheme = when (season) {
    Season.SPRING -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF2F6B45), primaryContainer = androidx.compose.ui.graphics.Color(0xFFD7EAD9))
    Season.SUMMER -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF176B72), primaryContainer = androidx.compose.ui.graphics.Color(0xFFCDECEF))
    Season.AUTUMN -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF8A5A20), primaryContainer = androidx.compose.ui.graphics.Color(0xFFF3E0C4))
    Season.WINTER -> lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF385B78), primaryContainer = androidx.compose.ui.graphics.Color(0xFFD8E7F3))
}

@Composable
fun ShjiratiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = schemeFor(currentSeason()), content = content)
}
