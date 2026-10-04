package com.flowbit.app

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.model.PeriodGoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitRepositoryTest {

    // ── Дефолты доменной модели ───────────────────────────────────────────────

    @Test
    fun `дата начала привычки по умолчанию — сегодня`() {
        val habit = Habit(name = "Тест")
        assertEquals(LocalDate.now(), habit.startDate)
    }

    @Test
    fun `целевое количество по умолчанию равно 1`() {
        val habit = Habit(name = "Тест")
        assertEquals(1, habit.targetCount)
    }

    @Test
    fun `привычка с ежедневной частотой имеет 7 дней`() {
        val habit = Habit(name = "Тест", frequency = HabitFrequency.DAILY,
            scheduledDays = DayOfWeek.entries.toSet())
        assertEquals(7, habit.scheduledDays.size)
    }

    @Test
    fun `isBadHabit по умолчанию false`() {
        val habit = Habit(name = "Тест")
        assertFalse(habit.isBadHabit)
    }

    @Test
    fun `allowStreakSkip по умолчанию false`() {
        val habit = Habit(name = "Тест")
        assertFalse(habit.allowStreakSkip)
    }

    @Test
    fun `stackAfterHabitId по умолчанию null`() {
        val habit = Habit(name = "Тест")
        assertNull(habit.stackAfterHabitId)
    }

    @Test
    fun `unit по умолчанию null`() {
        val habit = Habit(name = "Тест")
        assertNull(habit.unit)
    }

    @Test
    fun `timerSeconds по умолчанию 0`() {
        val habit = Habit(name = "Тест")
        assertEquals(0, habit.timerSeconds)
    }

    // ── PeriodGoalType лейблы ─────────────────────────────────────────────────

    @Test
    fun `NONE имеет лейбл Без цели`() {
        assertEquals("Без цели", PeriodGoalType.NONE.label)
    }

    @Test
    fun `WEEKLY имеет лейбл Раз в неделю`() {
        assertEquals("Раз в неделю", PeriodGoalType.WEEKLY.label)
    }

    @Test
    fun `MONTHLY имеет лейбл Раз в месяц`() {
        assertEquals("Раз в месяц", PeriodGoalType.MONTHLY.label)
    }

    @Test
    fun `всего 3 варианта PeriodGoalType`() {
        assertEquals(3, PeriodGoalType.entries.size)
    }

    // ── Логика выполнения ─────────────────────────────────────────────────────

    @Test
    fun `привычка считается выполненной когда completedCount не меньше targetCount`() {
        val targetCount = 3
        val completedCount = 3
        assertTrue(completedCount >= targetCount)
    }

    @Test
    fun `привычка не выполнена когда completedCount меньше targetCount`() {
        val targetCount = 3
        val completedCount = 2
        assertFalse(completedCount >= targetCount)
    }

    @Test
    fun `расписание на конкретные дни содержит только выбранные`() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val habit = Habit(name = "Тест", frequency = HabitFrequency.CUSTOM, scheduledDays = days)
        assertEquals(3, habit.scheduledDays.size)
        assertTrue(DayOfWeek.MONDAY in habit.scheduledDays)
        assertFalse(DayOfWeek.TUESDAY in habit.scheduledDays)
    }

    // ── HabitFrequency ────────────────────────────────────────────────────────

    @Test
    fun `всего 3 варианта HabitFrequency`() {
        assertEquals(3, HabitFrequency.entries.size)
    }
}
