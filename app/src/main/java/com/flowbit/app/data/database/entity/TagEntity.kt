package com.flowbit.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flowbit.app.domain.model.HabitTag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#4A90E2",
) {
    fun toDomain() = HabitTag(id = id, name = name, colorHex = colorHex)

    companion object {
        fun fromDomain(tag: HabitTag) = TagEntity(id = tag.id, name = tag.name, colorHex = tag.colorHex)
    }
}
