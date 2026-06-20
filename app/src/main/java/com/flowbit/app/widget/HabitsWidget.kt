package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.data.database.entity.HabitEntryEntity
import com.flowbit.app.domain.model.Habit
import com.flowbit.app.presentation.MainActivity
import com.flowbit.app.presentation.theme.DarkColorScheme
import com.flowbit.app.presentation.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HabitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .database()

        val habits = db.habitDao().getWidgetHabits().first().map { it.toDomain() }
        val today = LocalDate.now().toString()
        val completedIds = db.habitDao().getEntriesForDate(today).first()
            .filter { it.completedCount > 0 }
            .map { it.habitId }
            .toSet()

        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                WidgetContent(
                    context = context,
                    habits = habits,
                    completedIds = completedIds,
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    habits: List<Habit>,
    completedIds: Set<Long>,
) {
    val openIntent = Intent(context, MainActivity::class.java)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity(openIntent)),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "Flowbit",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.height(8.dp))
        if (habits.isEmpty()) {
            Text(
                text = "Нет привычек для виджета",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
            )
        } else {
            habits.forEach { habit ->
                val isDone = habit.id in completedIds
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = habit.emoji,
                        style = TextStyle(fontSize = 18.sp),
                        modifier = GlanceModifier.width(28.dp),
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = habit.name,
                        style = TextStyle(
                            color = if (isDone) GlanceTheme.colors.primary
                                    else GlanceTheme.colors.onSurface,
                            fontSize = 13.sp,
                        ),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Text(
                        text = if (isDone) "✓" else "○",
                        style = TextStyle(
                            color = if (isDone) GlanceTheme.colors.primary
                                    else GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp,
                        ),
                    )
                }
            }
        }
    }
}
