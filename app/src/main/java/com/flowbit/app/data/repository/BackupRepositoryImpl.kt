package com.flowbit.app.data.repository

import android.content.Context
import android.net.Uri
import com.flowbit.app.data.backup.HabitSerializer
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.domain.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val reminderDao: ReminderDao,
) : BackupRepository {

    override suspend fun exportJson(uri: Uri) {
        val habits = habitDao.getAllHabitsList()
        val entries = habitDao.getAllEntries()
        val reminders = reminderDao.getAllReminders()

        val root = JSONObject().apply {
            put("version", 2)
            put("exportedAt", LocalDate.now().toString())
            put("habits", JSONArray().apply { habits.forEach { put(HabitSerializer.habitToJson(it)) } })
            put("entries", JSONArray().apply { entries.forEach { put(HabitSerializer.entryToJson(it)) } })
            put("reminders", JSONArray().apply { reminders.forEach { put(HabitSerializer.reminderToJson(it)) } })
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: error("Не удалось открыть поток для записи")
    }

    override suspend fun importJson(uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            ?: error("Не удалось открыть файл")

        val root = JSONObject(json)
        val habitsJson   = root.getJSONArray("habits")
        val entriesJson  = root.getJSONArray("entries")
        val remindersJson = root.optJSONArray("reminders")

        val habitEntities = (0 until habitsJson.length()).map { HabitSerializer.jsonToHabit(habitsJson.getJSONObject(it)) }
        val entryEntities = (0 until entriesJson.length()).map { HabitSerializer.jsonToEntry(entriesJson.getJSONObject(it)) }

        habitDao.insertAllHabits(habitEntities)
        habitDao.insertAllEntries(entryEntities)

        if (remindersJson != null) {
            for (i in 0 until remindersJson.length()) {
                reminderDao.insertReminder(HabitSerializer.jsonToReminder(remindersJson.getJSONObject(i)))
            }
        }
    }

    override suspend fun exportCsv(uri: Uri) {
        val habits  = habitDao.getAllHabitsList().associateBy { it.id }
        val entries = habitDao.getAllEntries()

        val sb = StringBuilder()
        sb.appendLine("habit_id,habit_name,date,completed_count,target_count,is_skipped,note,marked_at")
        entries.forEach { e ->
            val h    = habits[e.habitId]
            val name = (h?.name ?: "").replace(",", ";").replace("\n", " ")
            val note = (e.note ?: "").replace(",", ";").replace("\n", " ")
            sb.appendLine("${e.habitId},\"$name\",${e.date},${e.completedCount},${h?.targetCount ?: 1},${e.isSkipped},\"$note\",${e.markedAt ?: ""}")
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(sb.toString().toByteArray(Charsets.UTF_8))
        } ?: error("Не удалось открыть поток для записи")
    }
}
