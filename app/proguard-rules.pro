# Базовые правила ProGuard для Flowbit

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Сохраняем модели данных
-keep class com.flowbit.app.domain.model.** { *; }
-keep class com.flowbit.app.data.database.entity.** { *; }
