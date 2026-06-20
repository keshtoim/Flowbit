package com.flowbit.app

import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitFrequency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitRepositoryTest {

    @Test
    fun `привычка с ежедневной частотой запланирована на любой день`() {
        val habit = Habit(
            name = "Тест",
            frequency = HabitFrequency.DAILY,
            scheduledDays = DayOfWeek.entries.toSet(),
        )
        assertEquals(HabitFrequency.DAILY, habit.frequency)
        assert(habit.scheduledDays.size == 7)
    }

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
}
