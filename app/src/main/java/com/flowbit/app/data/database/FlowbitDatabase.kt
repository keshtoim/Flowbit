package com.flowbit.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.data.database.dao.TagDao
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.data.database.entity.ReminderEntity
import com.flowbit.app.data.database.entity.TagEntity

@Database(
    entities = [HabitEntity::class, HabitEntryEntity::class, ReminderEntity::class, TagEntity::class],
    version = 11,
    exportSchema = false,
)
abstract class FlowbitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun reminderDao(): ReminderDao
    abstract fun tagDao(): TagDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN isPhotoHidden INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE habits ADD COLUMN audioUri TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habit_entries ADD COLUMN note TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS tags (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, colorHex TEXT NOT NULL DEFAULT '#4A90E2')"
                )
                database.execSQL("ALTER TABLE habits ADD COLUMN tagId INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN periodGoalType TEXT NOT NULL DEFAULT 'NONE'")
                database.execSQL("ALTER TABLE habits ADD COLUMN periodGoalCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habit_entries ADD COLUMN isSkipped INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN unit TEXT")
                database.execSQL("ALTER TABLE habits ADD COLUMN timerSeconds INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habit_entries ADD COLUMN markedAt TEXT")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN isBadHabit INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
