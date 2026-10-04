package com.flowbit.app.data.backup

import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.data.database.entity.ReminderEntity
import org.json.JSONObject

internal object HabitSerializer {

    fun habitToJson(h: HabitEntity): JSONObject = JSONObject().apply {
        put("id", h.id)
        put("name", h.name)
        put("emoji", h.emoji)
        put("colorHex", h.colorHex)
        put("targetCount", h.targetCount)
        put("frequency", h.frequency)
        put("scheduledDays", h.scheduledDays)
        put("startDate", h.startDate)
        put("isArchived", h.isArchived)
        put("showInWidget", h.showInWidget)
        put("createdAt", h.createdAt)
        put("sortOrder", h.sortOrder)
        put("isPhotoHidden", h.isPhotoHidden)
        put("periodGoalType", h.periodGoalType)
        put("periodGoalCount", h.periodGoalCount)
        put("timerSeconds", h.timerSeconds)
        put("isBadHabit", h.isBadHabit)
        put("allowStreakSkip", h.allowStreakSkip)
        h.stackAfterHabitId?.let { put("stackAfterHabitId", it) }
        h.photoUri?.let { put("photoUri", it) }
        h.audioUri?.let { put("audioUri", it) }
        h.tagId?.let { put("tagId", it) }
        h.unit?.let { put("unit", it) }
    }

    fun jsonToHabit(j: JSONObject): HabitEntity = HabitEntity(
        id = j.getLong("id"),
        name = j.getString("name"),
        emoji = j.getString("emoji"),
        colorHex = j.getString("colorHex"),
        targetCount = j.getInt("targetCount"),
        frequency = j.getString("frequency"),
        scheduledDays = j.optString("scheduledDays", ""),
        startDate = j.getString("startDate"),
        isArchived = j.getBoolean("isArchived"),
        showInWidget = j.getBoolean("showInWidget"),
        createdAt = j.getString("createdAt"),
        sortOrder = j.optInt("sortOrder", 0),
        isPhotoHidden = j.optBoolean("isPhotoHidden", false),
        periodGoalType = j.optString("periodGoalType", "NONE"),
        periodGoalCount = j.optInt("periodGoalCount", 0),
        timerSeconds = j.optInt("timerSeconds", 0),
        isBadHabit = j.optBoolean("isBadHabit", false),
        allowStreakSkip = j.optBoolean("allowStreakSkip", false),
        stackAfterHabitId = if (j.has("stackAfterHabitId")) j.getLong("stackAfterHabitId") else null,
        photoUri = j.optString("photoUri").takeIf { it.isNotEmpty() },
        audioUri = j.optString("audioUri").takeIf { it.isNotEmpty() },
        tagId = if (j.has("tagId")) j.getLong("tagId") else null,
        unit = j.optString("unit").takeIf { it.isNotEmpty() },
    )

    fun entryToJson(e: HabitEntryEntity): JSONObject = JSONObject().apply {
        put("id", e.id)
        put("habitId", e.habitId)
        put("date", e.date)
        put("completedCount", e.completedCount)
        put("isSkipped", e.isSkipped)
        e.note?.let { put("note", it) }
        e.markedAt?.let { put("markedAt", it) }
    }

    fun jsonToEntry(j: JSONObject): HabitEntryEntity = HabitEntryEntity(
        id = j.getLong("id"),
        habitId = j.getLong("habitId"),
        date = j.getString("date"),
        completedCount = j.getInt("completedCount"),
        isSkipped = j.optBoolean("isSkipped", false),
        note = j.optString("note").takeIf { it.isNotEmpty() },
        markedAt = j.optString("markedAt").takeIf { it.isNotEmpty() },
    )

    fun reminderToJson(r: ReminderEntity): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("habitId", r.habitId)
        put("timeHour", r.timeHour)
        put("timeMinute", r.timeMinute)
        put("isEnabled", r.isEnabled)
    }

    fun jsonToReminder(j: JSONObject): ReminderEntity = ReminderEntity(
        id = j.getLong("id"),
        habitId = j.getLong("habitId"),
        timeHour = j.getInt("timeHour"),
        timeMinute = j.getInt("timeMinute"),
        isEnabled = j.getBoolean("isEnabled"),
    )
}
