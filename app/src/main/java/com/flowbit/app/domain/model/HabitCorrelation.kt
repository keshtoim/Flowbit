package com.flowbit.app.domain.model

data class HabitCorrelation(
    val habitA: String,
    val habitB: String,
    val emojiA: String,
    val emojiB: String,
    val sharedDays: Int,
    val totalDays: Int,
) {
    val rate: Float get() = if (totalDays > 0) sharedDays.toFloat() / totalDays else 0f
}
