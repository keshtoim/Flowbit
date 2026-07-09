package com.flowbit.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: Long = 0,
    val name: String,
    val emoji: String = "✅",
    val color: HabitColor = HabitColor.TEAL,
    val targetCount: Int = 1,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val scheduledDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminders: List<HabitReminder> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    val isArchived: Boolean = false,
    val showInWidget: Boolean = false,
    val createdAt: LocalDate = LocalDate.now(),
    val sortOrder: Int = 0,
    val photoUri: String? = null,
    val isPhotoHidden: Boolean = false,
    val audioUri: String? = null,
    val tagId: Long? = null,
    val periodGoalType: PeriodGoalType = PeriodGoalType.NONE,
    val periodGoalCount: Int = 0,
)

data class HabitReminder(
    val id: Long = 0,
    val habitId: Long = 0,
    val time: LocalTime,
    val isEnabled: Boolean = true,
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    CUSTOM,
}

enum class PeriodGoalType(val label: String) {
    NONE("Без цели"),
    WEEKLY("Раз в неделю"),
    MONTHLY("Раз в месяц"),
}

enum class HabitColor(val hex: String) {
    TEAL("#00E5C0"),
    BLUE("#4A90E2"),
    PURPLE("#9B59B6"),
    GREEN("#2ECC71"),
    ORANGE("#E67E22"),
    RED("#E74C3C"),
    PINK("#FF69B4"),
    YELLOW("#F1C40F"),
}
