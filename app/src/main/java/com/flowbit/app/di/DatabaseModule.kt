package com.flowbit.app.di

import android.content.Context
import androidx.room.Room
import com.flowbit.app.data.database.FlowbitDatabase
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.dao.ReminderDao
import com.flowbit.app.data.database.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlowbitDatabase =
        Room.databaseBuilder(context, FlowbitDatabase::class.java, "flowbit.db")
            .addMigrations(FlowbitDatabase.MIGRATION_1_2, FlowbitDatabase.MIGRATION_2_3, FlowbitDatabase.MIGRATION_3_4, FlowbitDatabase.MIGRATION_4_5, FlowbitDatabase.MIGRATION_5_6, FlowbitDatabase.MIGRATION_6_7)
            .build()

    @Provides
    fun provideHabitDao(db: FlowbitDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideReminderDao(db: FlowbitDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideTagDao(db: FlowbitDatabase): TagDao = db.tagDao()
}
