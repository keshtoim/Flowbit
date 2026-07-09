package com.flowbit.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowbit.app.domain.model.HabitEntry
import java.time.LocalDate

@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("habitId"), Index("date")],
)
data class HabitEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completedCount: Int,
    val note: String? = null,
) {
    fun toDomain(): HabitEntry = HabitEntry(
        id = id,
        habitId = habitId,
        date = LocalDate.parse(date),
        completedCount = completedCount,
        note = note,
    )

    companion object {
        fun fromDomain(entry: HabitEntry): HabitEntryEntity = HabitEntryEntity(
            id = entry.id,
            habitId = entry.habitId,
            date = entry.date.toString(),
            completedCount = entry.completedCount,
            note = entry.note,
        )
    }
}
