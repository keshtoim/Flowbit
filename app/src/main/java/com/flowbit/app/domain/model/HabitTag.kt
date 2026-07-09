package com.flowbit.app.domain.model

data class HabitTag(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#4A90E2",
)

enum class GroupingMode(val label: String) {
    NONE("Без группировки"),
    BY_TAG("По тегам"),
    BY_FREQUENCY("По частоте"),
    BY_STATUS("По статусу"),
}
