package com.flowbit.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowbit.app.domain.model.HabitReminder
import java.time.LocalTime

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("habitId")],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val timeHour: Int,
    val timeMinute: Int,
    val isEnabled: Boolean,
) {
    fun toDomain(): HabitReminder = HabitReminder(
        id = id,
        habitId = habitId,
        time = LocalTime.of(timeHour, timeMinute),
        isEnabled = isEnabled,
    )

    companion object {
        fun fromDomain(reminder: HabitReminder): ReminderEntity = ReminderEntity(
            id = reminder.id,
            habitId = reminder.habitId,
            timeHour = reminder.time.hour,
            timeMinute = reminder.time.minute,
            isEnabled = reminder.isEnabled,
        )
    }
}
