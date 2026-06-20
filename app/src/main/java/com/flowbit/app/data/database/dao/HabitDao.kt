package com.flowbit.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE showInWidget = 1 AND isArchived = 0")
    fun getWidgetHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    fun getEntriesForHabit(habitId: Long): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE date = :date")
    fun getEntriesForDate(date: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntryForDate(habitId: Long, date: String): HabitEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: HabitEntryEntity)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getAllEntriesForHabit(habitId: Long): List<HabitEntryEntity>
}
