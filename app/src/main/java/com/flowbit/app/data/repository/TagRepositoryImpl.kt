package com.flowbit.app.data.repository

import com.flowbit.app.data.database.dao.TagDao
import com.flowbit.app.data.database.entity.TagEntity
import com.flowbit.app.domain.model.HabitTag
import com.flowbit.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao,
) : TagRepository {
    override fun getAllTags(): Flow<List<HabitTag>> =
        dao.getAllTags().map { list -> list.map { it.toDomain() } }

    override suspend fun getTagById(id: Long): HabitTag? =
        dao.getTagById(id)?.toDomain()

    override suspend fun insertTag(tag: HabitTag): Long =
        dao.insertTag(TagEntity.fromDomain(tag))

    override suspend fun updateTag(tag: HabitTag) =
        dao.updateTag(TagEntity.fromDomain(tag))

    override suspend fun deleteTag(tag: HabitTag) =
        dao.deleteTag(TagEntity.fromDomain(tag))
}
