package com.flowbit.app

import com.flowbit.app.domain.model.HabitStats
import com.flowbit.app.domain.usecase.stats.GetBestTimeUseCase
import com.flowbit.app.domain.usecase.stats.GetHabitCorrelationsUseCase
import com.flowbit.app.domain.usecase.stats.GetPeriodComparisonUseCase
import com.flowbit.app.domain.usecase.stats.GetWeekdayInsightUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsUseCaseTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun stat(id: Long, dates: List<LocalDate>) = HabitStats(
        habitId = id,
        habitName = "Привычка $id",
        habitEmoji = "✅",
        completionRate = if (dates.isEmpty()) 0f else 0.5f,
        currentStreak = 0,
        longestStreak = 0,
        totalCompletions = dates.size,
        completedDates = dates,
    )

    // ── GetBestTimeUseCase ────────────────────────────────────────────────────

    @Test
    fun `GetBestTime — пустой список возвращает null`() {
        assertNull(GetBestTimeUseCase()(emptyList()))
    }

    @Test
    fun `GetBestTime — только null-элементы возвращает null`() {
        assertNull(GetBestTimeUseCase()(listOf(null, null)))
    }

    @Test
    fun `GetBestTime — определяет пиковый час`() {
        val times = listOf("08:30", "08:45", "08:00", "21:00", "08:15")
        val result = GetBestTimeUseCase()(times)
        assertNotNull(result)
        assertEquals(8, result!!.peakHour)
        assertEquals(4, result.hourCounts[8])
        assertEquals(1, result.hourCounts[21])
    }

    @Test
    fun `GetBestTime — некорректные строки игнорируются`() {
        val times = listOf("abc", ":", null, "07:00", "07:30")
        val result = GetBestTimeUseCase()(times)
        assertNotNull(result)
        assertEquals(7, result!!.peakHour)
    }

    // ── GetHabitCorrelationsUseCase ───────────────────────────────────────────

    @Test
    fun `GetHabitCorrelations — менее 2 привычек возвращает пустой список`() {
        val result = GetHabitCorrelationsUseCase()(listOf(stat(1L, emptyList())))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetHabitCorrelations — пересечение менее 7 дней отфильтровывается`() {
        val days = (1..5).map { LocalDate.of(2026, 1, it) }
        val result = GetHabitCorrelationsUseCase()(listOf(stat(1L, days), stat(2L, days)))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetHabitCorrelations — корреляция найдена при достаточном пересечении`() {
        val days = (1..20).map { LocalDate.of(2026, 1, it) }
        val result = GetHabitCorrelationsUseCase()(listOf(stat(1L, days), stat(2L, days)))
        assertEquals(1, result.size)
        assertEquals(20, result[0].sharedDays)
        assertEquals(20, result[0].totalDays)
        assertEquals(1f, result[0].rate, 0.001f)
    }

    @Test
    fun `GetHabitCorrelations — результат отсортирован по убыванию rate`() {
        val allDays = (1..20).map { LocalDate.of(2026, 2, it) }
        val halfDays = (1..10).map { LocalDate.of(2026, 2, it) }
        val stats = listOf(
            stat(1L, allDays),
            stat(2L, allDays),
            stat(3L, halfDays),
        )
        val result = GetHabitCorrelationsUseCase()(stats)
        assertTrue(result.size >= 2)
        assertTrue(result[0].rate >= result[1].rate)
    }

    @Test
    fun `GetHabitCorrelations — возвращает не более 3 результатов`() {
        val days = (1..20).map { LocalDate.of(2026, 3, it) }
        val stats = (1L..5L).map { stat(it, days) }
        val result = GetHabitCorrelationsUseCase()(stats)
        assertTrue(result.size <= 3)
    }

    // ── GetWeekdayInsightUseCase ──────────────────────────────────────────────

    @Test
    fun `GetWeekdayInsight — пустой список возвращает null`() {
        assertNull(GetWeekdayInsightUseCase()(emptyList()))
    }

    @Test
    fun `GetWeekdayInsight — возвращает результат для непустого списка`() {
        val today = LocalDate.now()
        val last30 = (0..29).map { today.minusDays(it.toLong()) }
        val weekdays = last30.filter { it.dayOfWeek.value <= 5 }
        val stat = stat(1L, weekdays)
        val result = GetWeekdayInsightUseCase()(listOf(stat))
        assertNotNull(result)
        assertTrue(result!!.weekdayRate > 0f)
    }

    @Test
    fun `GetWeekdayInsight — текст содержит проценты`() {
        val today = LocalDate.now()
        val dates = (0..29).map { today.minusDays(it.toLong()) }
        val result = GetWeekdayInsightUseCase()(listOf(stat(1L, dates)))
        assertNotNull(result)
        assertTrue(result!!.insightText.contains("%"))
    }

    // ── GetPeriodComparisonUseCase ────────────────────────────────────────────

    @Test
    fun `GetPeriodComparison — пустой список возвращает null`() {
        assertNull(GetPeriodComparisonUseCase()(emptyList()))
    }

    @Test
    fun `GetPeriodComparison — thisWeekRate в диапазоне от 0 до 1`() {
        val today = LocalDate.now()
        val dates = (0..6).map { today.minusDays(it.toLong()) }
        val result = GetPeriodComparisonUseCase()(listOf(stat(1L, dates)))
        assertNotNull(result)
        assertTrue(result!!.thisWeekRate in 0f..1f)
        assertTrue(result.lastWeekRate in 0f..1f)
        assertTrue(result.thisMonthRate in 0f..1f)
        assertTrue(result.lastMonthRate in 0f..1f)
    }

    @Test
    fun `GetPeriodComparison — метки недели содержат разделитель`() {
        val today = LocalDate.now()
        val dates = (0..6).map { today.minusDays(it.toLong()) }
        val result = GetPeriodComparisonUseCase()(listOf(stat(1L, dates)))
        assertNotNull(result)
        assertTrue(result!!.thisWeekLabel.contains("–"))
        assertTrue(result.lastWeekLabel.contains("–"))
    }

    @Test
    fun `GetPeriodComparison — нет дат в прошлом месяце даёт lastMonthRate=0`() {
        val today = LocalDate.now()
        val thisMonthDates = (0 until today.dayOfMonth).map { today.minusDays(it.toLong()) }
        val result = GetPeriodComparisonUseCase()(listOf(stat(1L, thisMonthDates)))
        assertNotNull(result)
        assertEquals(0f, result!!.lastMonthRate, 0.001f)
    }
}
