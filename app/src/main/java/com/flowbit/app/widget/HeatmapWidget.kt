package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.flowbit.app.presentation.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

class HeatmapWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val db = ep.database()

        val today = LocalDate.now()
        val habits = db.habitDao().getActiveHabits().first()
        val habitCount = habits.size.coerceAtLeast(1)

        // Последние 18 недель
        val weeksCount = 18
        val startDate = run {
            val base = today.minusWeeks(weeksCount.toLong() - 1)
            val shift = (base.dayOfWeek.value - DayOfWeek.MONDAY.value).let { if (it < 0) it + 7 else it }
            base.minusDays(shift.toLong())
        }

        // Собираем все записи за период одним запросом
        val entries = db.habitDao()
            .getEntriesForDateRange(startDate.toString(), today.toString())
            .groupBy { it.date }

        // Для каждой даты считаем rate (выполнено / всего привычек)
        val rateByDate = mutableMapOf<LocalDate, Float>()
        var d = startDate
        while (!d.isAfter(today)) {
            val dayEntries = entries[d.toString()] ?: emptyList()
            val done = dayEntries.count { it.completedCount >= 1 }
            rateByDate[d] = done.toFloat() / habitCount
            d = d.plusDays(1)
        }

        val glanceColors = buildGlanceColors(context)

        provideContent {
            GlanceTheme(colors = glanceColors) {
                val bg = GlanceTheme.colors.surface
                val primary = GlanceTheme.colors.primary
                val surfaceVariant = GlanceTheme.colors.surfaceVariant
                val onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .cornerRadius(20.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                        .padding(12.dp),
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Text(
                            text = "Активность",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceVariant,
                            ),
                        )
                        Spacer(GlanceModifier.height(6.dp))

                        // Сетка: 7 строк × weeksCount колонок
                        val cellDp = 9.dp
                        val gapDp = 2.dp

                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            for (dow in 0..6) {
                                Row {
                                    for (week in 0 until weeksCount) {
                                        val date = startDate
                                            .plusWeeks(week.toLong())
                                            .plusDays(dow.toLong())
                                        val isFuture = date.isAfter(today)
                                        val rate = if (isFuture) 0f else rateByDate[date] ?: 0f

                                        val alpha = when {
                                            isFuture -> 0.1f
                                            rate <= 0f -> 0.15f
                                            rate < 0.4f -> 0.4f
                                            rate < 0.7f -> 0.65f
                                            else -> 1.0f
                                        }

                                        val cellColor = if (isFuture || rate <= 0f)
                                            surfaceVariant
                                        else
                                            ColorProvider(
                                                androidx.compose.ui.graphics.lerp(
                                                    primary.getColor(context).copy(alpha = 0.3f),
                                                    primary.getColor(context),
                                                    alpha,
                                                )
                                            )

                                        Box(
                                            modifier = GlanceModifier
                                                .size(cellDp)
                                                .cornerRadius(2.dp)
                                                .background(cellColor),
                                        ) {}

                                        if (week < weeksCount - 1) {
                                            Spacer(GlanceModifier.width(gapDp))
                                        }
                                    }
                                }
                                if (dow < 6) {
                                    Spacer(GlanceModifier.height(gapDp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class HeatmapWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapWidget()
}
