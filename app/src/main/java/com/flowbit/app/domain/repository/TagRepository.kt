package com.flowbit.app.domain.repository

import com.flowbit.app.domain.model.HabitTag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<HabitTag>>
    suspend fun getTagById(id: Long): HabitTag?
    suspend fun insertTag(tag: HabitTag): Long
    suspend fun updateTag(tag: HabitTag)
    suspend fun deleteTag(tag: HabitTag)
}
