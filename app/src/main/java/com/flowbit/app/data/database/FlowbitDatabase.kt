package com.flowbit.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.data.database.entity.ReminderEntity

@Database(
    entities = [HabitEntity::class, HabitEntryEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FlowbitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun reminderDao(): ReminderDao
}
