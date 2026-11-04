package com.flowbit.app.domain.model

data class HabitTag(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#4A90E2",
)

enum class GroupingMode {
    NONE,
    BY_TAG,
    BY_FREQUENCY,
    BY_STATUS,
}
