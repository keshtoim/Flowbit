package com.flowbit.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.domain.model.HabitColor
import com.flowbit.app.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val targetCount: Int,
    val frequency: String,
    val scheduledDays: String,
    val startDate: String,
    val isArchived: Boolean,
    val showInWidget: Boolean,
    val createdAt: String,
    val sortOrder: Int = 0,
    val photoUri: String? = null,
    val isPhotoHidden: Boolean = false,
    val audioUri: String? = null,
    val tagId: Long? = null,
) {
    fun toDomain(): Habit = Habit(
        id = id,
        name = name,
        emoji = emoji,
        color = HabitColor.entries.find { it.hex == colorHex } ?: HabitColor.TEAL,
        targetCount = targetCount,
        frequency = HabitFrequency.valueOf(frequency),
        scheduledDays = scheduledDays.split(",")
            .filter { it.isNotBlank() }
            .map { DayOfWeek.valueOf(it) }
            .toSet(),
        startDate = LocalDate.parse(startDate),
        isArchived = isArchived,
        showInWidget = showInWidget,
        createdAt = LocalDate.parse(createdAt),
        sortOrder = sortOrder,
        photoUri = photoUri,
        isPhotoHidden = isPhotoHidden,
        audioUri = audioUri,
        tagId = tagId,
    )

    companion object {
        fun fromDomain(habit: Habit): HabitEntity = HabitEntity(
            id = habit.id,
            name = habit.name,
            emoji = habit.emoji,
            colorHex = habit.color.hex,
            targetCount = habit.targetCount,
            frequency = habit.frequency.name,
            scheduledDays = habit.scheduledDays.joinToString(",") { it.name },
            startDate = habit.startDate.toString(),
            isArchived = habit.isArchived,
            showInWidget = habit.showInWidget,
            createdAt = habit.createdAt.toString(),
            sortOrder = habit.sortOrder,
            photoUri = habit.photoUri,
            isPhotoHidden = habit.isPhotoHidden,
            audioUri = habit.audioUri,
            tagId = habit.tagId,
        )
    }
}
