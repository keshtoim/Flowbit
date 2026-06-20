package com.flowbit.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flowbit.app.data.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    fun getRemindersForHabit(habitId: Long): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE habitId = :habitId")
    suspend fun deleteRemindersForHabit(habitId: Long)

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getAllActiveReminders(): List<ReminderEntity>
}
