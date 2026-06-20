package com.flowbit.app.widget

import com.flowbit.app.data.database.FlowbitDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun database(): FlowbitDatabase
}
