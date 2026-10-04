package com.flowbit.app

import com.flowbit.app.data.backup.HabitSerializer
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.data.database.entity.ReminderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitSerializerTest {

    private fun baseHabit() = HabitEntity(
        id = 1L,
        name = "Бег",
        emoji = "🏃",
        colorHex = "#FF6B4A",
        targetCount = 1,
        frequency = "DAILY",
        scheduledDays = "",
        startDate = "2026-01-01",
        isArchived = false,
        showInWidget = true,
        createdAt = "2026-01-01",
    )

    // ── isBadHabit ────────────────────────────────────────────────────────────

    @Test
    fun `isBadHabit=true сохраняется и восстанавливается`() {
        val original = baseHabit().copy(isBadHabit = true)
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))
        assertEquals(true, restored.isBadHabit)
    }

    @Test
    fun `isBadHabit=false сохраняется и восстанавливается`() {
        val original = baseHabit().copy(isBadHabit = false)
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))
        assertEquals(false, restored.isBadHabit)
    }

    @Test
    fun `isBadHabit по умолчанию false при импорте старых данных без этого поля`() {
        val json = HabitSerializer.habitToJson(baseHabit())
        json.remove("isBadHabit")
        val restored = HabitSerializer.jsonToHabit(json)
        assertEquals(false, restored.isBadHabit)
    }

    // ── allowStreakSkip ───────────────────────────────────────────────────────

    @Test
    fun `allowStreakSkip=true сохраняется и восстанавливается`() {
        val original = baseHabit().copy(allowStreakSkip = true)
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))
        assertEquals(true, restored.allowStreakSkip)
    }

    @Test
    fun `allowStreakSkip по умолчанию false при импорте старых данных`() {
        val json = HabitSerializer.habitToJson(baseHabit())
        json.remove("allowStreakSkip")
        val restored = HabitSerializer.jsonToHabit(json)
        assertEquals(false, restored.allowStreakSkip)
    }

    // ── stackAfterHabitId ────────────────────────────────────────────────────

    @Test
    fun `stackAfterHabitId сохраняется и восстанавливается`() {
        val original = baseHabit().copy(stackAfterHabitId = 42L)
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))
        assertEquals(42L, restored.stackAfterHabitId)
    }

    @Test
    fun `stackAfterHabitId null восстанавливается как null`() {
        val original = baseHabit().copy(stackAfterHabitId = null)
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))
        assertNull(restored.stackAfterHabitId)
    }

    // ── Полный round-trip ─────────────────────────────────────────────────────

    @Test
    fun `полный round-trip со всеми полями`() {
        val original = HabitEntity(
            id = 7L,
            name = "Медитация",
            emoji = "🧘",
            colorHex = "#9B7FF5",
            targetCount = 2,
            frequency = "CUSTOM",
            scheduledDays = "MONDAY,WEDNESDAY,FRIDAY",
            startDate = "2026-03-15",
            isArchived = false,
            showInWidget = false,
            createdAt = "2026-03-15",
            sortOrder = 3,
            isPhotoHidden = true,
            periodGoalType = "WEEKLY",
            periodGoalCount = 5,
            timerSeconds = 600,
            isBadHabit = false,
            allowStreakSkip = true,
            stackAfterHabitId = 2L,
            tagId = 1L,
            unit = "мин",
        )
        val restored = HabitSerializer.jsonToHabit(HabitSerializer.habitToJson(original))

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.emoji, restored.emoji)
        assertEquals(original.colorHex, restored.colorHex)
        assertEquals(original.targetCount, restored.targetCount)
        assertEquals(original.frequency, restored.frequency)
        assertEquals(original.scheduledDays, restored.scheduledDays)
        assertEquals(original.startDate, restored.startDate)
        assertEquals(original.isArchived, restored.isArchived)
        assertEquals(original.showInWidget, restored.showInWidget)
        assertEquals(original.sortOrder, restored.sortOrder)
        assertEquals(original.isPhotoHidden, restored.isPhotoHidden)
        assertEquals(original.periodGoalType, restored.periodGoalType)
        assertEquals(original.periodGoalCount, restored.periodGoalCount)
        assertEquals(original.timerSeconds, restored.timerSeconds)
        assertEquals(original.isBadHabit, restored.isBadHabit)
        assertEquals(original.allowStreakSkip, restored.allowStreakSkip)
        assertEquals(original.stackAfterHabitId, restored.stackAfterHabitId)
        assertEquals(original.tagId, restored.tagId)
        assertEquals(original.unit, restored.unit)
    }

    // ── HabitEntryEntity ──────────────────────────────────────────────────────

    @Test
    fun `запись (entry) сохраняется и восстанавливается`() {
        val original = HabitEntryEntity(
            id = 10L,
            habitId = 1L,
            date = "2026-10-01",
            completedCount = 3,
            isSkipped = false,
            note = "Хорошо прошло",
            markedAt = "2026-10-01T08:00:00",
        )
        val restored = HabitSerializer.jsonToEntry(HabitSerializer.entryToJson(original))

        assertEquals(original.id, restored.id)
        assertEquals(original.habitId, restored.habitId)
        assertEquals(original.date, restored.date)
        assertEquals(original.completedCount, restored.completedCount)
        assertEquals(original.isSkipped, restored.isSkipped)
        assertEquals(original.note, restored.note)
        assertEquals(original.markedAt, restored.markedAt)
    }

    @Test
    fun `запись с isSkipped=true сохраняется`() {
        val original = HabitEntryEntity(
            id = 11L, habitId = 1L, date = "2026-10-02",
            completedCount = 0, isSkipped = true,
        )
        val restored = HabitSerializer.jsonToEntry(HabitSerializer.entryToJson(original))
        assertEquals(true, restored.isSkipped)
        assertEquals(0, restored.completedCount)
    }

    // ── ReminderEntity ────────────────────────────────────────────────────────

    @Test
    fun `напоминание сохраняется и восстанавливается`() {
        val original = ReminderEntity(
            id = 5L, habitId = 1L,
            timeHour = 8, timeMinute = 30, isEnabled = true,
        )
        val restored = HabitSerializer.jsonToReminder(HabitSerializer.reminderToJson(original))

        assertEquals(original.id, restored.id)
        assertEquals(original.habitId, restored.habitId)
        assertEquals(original.timeHour, restored.timeHour)
        assertEquals(original.timeMinute, restored.timeMinute)
        assertEquals(original.isEnabled, restored.isEnabled)
    }
}
