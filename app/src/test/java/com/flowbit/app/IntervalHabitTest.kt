package com.flowbit.app

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitEntry
import com.flowbit.app.domain.model.HabitFrequency
import com.flowbit.app.domain.usecase.habit.HabitForDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IntervalHabitTest {

    private val today = LocalDate.of(2026, 11, 4)
    private val habit = Habit(
        id = 1,
        name = "Тренировка",
        frequency = HabitFrequency.INTERVAL,
        intervalTimes = 3,
        intervalDays = 7,
        startDate = today.minusDays(30),
    )

    private fun done(daysAgo: Long) = HabitEntry(habitId = 1, date = today.minusDays(daysAgo), completedCount = 1)

    @Test
    fun `интервальная привычка запланирована на каждый день`() {
        assertTrue((0L..6L).all { habit.isScheduledOn(today.plusDays(it)) })
    }

    @Test
    fun `в окно попадают только предыдущие intervalDays-1 дней`() {
        val entries = listOf(done(0), done(1), done(6), done(7))
        assertEquals(2, habit.intervalCreditsBefore(today, entries))
    }

    @Test
    fun `обычный пропуск не засчитывается, заморозка засчитывается`() {
        val entries = listOf(
            done(1).copy(isSkipped = true),
            HabitEntry(habitId = 1, date = today.minusDays(2), isFrozen = true),
        )
        assertEquals(1, habit.intervalCreditsBefore(today, entries))
    }

    @Test
    fun `норма набрана за прошлые дни — день считается выполненным`() {
        val hfd = HabitForDate(habit, today, entry = null, isScheduledForDate = true, intervalCredits = 3)
        assertTrue(hfd.isDone)
        assertEquals(3, hfd.intervalProgress)
    }

    @Test
    fun `норма не набрана и сегодня не отмечено — не выполнено`() {
        val hfd = HabitForDate(habit, today, entry = null, isScheduledForDate = true, intervalCredits = 2)
        assertFalse(hfd.isDone)
    }

    @Test
    fun `отметка сегодня увеличивает прогресс окна`() {
        val hfd = HabitForDate(habit, today, entry = done(0), isScheduledForDate = true, intervalCredits = 2)
        assertTrue(hfd.isDone)
        assertEquals(3, hfd.intervalProgress)
    }

    @Test
    fun `покрытие окном не действует для ежедневной привычки`() {
        val daily = habit.copy(frequency = HabitFrequency.DAILY)
        val hfd = HabitForDate(daily, today, entry = null, isScheduledForDate = true, intervalCredits = 5)
        assertFalse(hfd.isDone)
    }
}
