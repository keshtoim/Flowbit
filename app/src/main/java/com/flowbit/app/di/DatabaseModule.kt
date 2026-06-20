package com.flowbit.app.di

import android.content.Context
import androidx.room.Room
import com.flowbit.app.data.database.FlowbitDatabase
import com.flowbit.app.data.database.dao.HabitDao
import com.flowbit.app.data.database.dao.ReminderDao
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
        Room.databaseBuilder(context, FlowbitDatabase::class.java, "flowbit.db").build()

    @Provides
    fun provideHabitDao(db: FlowbitDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideReminderDao(db: FlowbitDatabase): ReminderDao = db.reminderDao()
}
