package com.moukim.shjirati.domain

import java.time.LocalDate

enum class Season { SPRING, SUMMER, AUTUMN, WINTER }

fun currentSeason(date: LocalDate = LocalDate.now()): Season = when (date.monthValue) {
    3, 4, 5 -> Season.SPRING
    6, 7, 8 -> Season.SUMMER
    9, 10, 11 -> Season.AUTUMN
    else -> Season.WINTER
}
