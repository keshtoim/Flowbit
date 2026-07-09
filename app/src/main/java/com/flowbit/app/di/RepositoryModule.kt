package com.flowbit.app.di

import com.flowbit.app.data.repository.HabitRepositoryImpl
import com.flowbit.app.data.repository.ReminderRepositoryImpl
import com.flowbit.app.data.repository.TagRepositoryImpl
import com.flowbit.app.domain.repository.HabitRepository
import com.flowbit.app.domain.repository.ReminderRepository
import com.flowbit.app.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
}
