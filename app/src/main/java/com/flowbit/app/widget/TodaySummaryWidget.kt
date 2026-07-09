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
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.flowbit.app.presentation.MainActivity
import com.flowbit.app.presentation.theme.DarkColorScheme
import com.flowbit.app.presentation.theme.LightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TodaySummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val db = ep.database()
        val today = LocalDate.now().toString()

        val habits = db.habitDao().getActiveHabits().first()
        val entries = db.habitDao().getEntriesForDate(today).first()
            .associateBy { it.habitId }

        val total = habits.size
        val done = habits.count { h ->
            (entries[h.id]?.completedCount ?: 0) >= h.targetCount
        }

        provideContent {
            GlanceTheme(colors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)) {
                val bg = GlanceTheme.colors.widgetBackground
                val primary = GlanceTheme.colors.primary
                val onSurface = GlanceTheme.colors.onSurface
                val onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant

                val emoji = when {
                    total == 0 -> "🌱"
                    done == total && total > 0 -> "🎉"
                    done.toFloat() / total.coerceAtLeast(1) >= 0.5f -> "💪"
                    else -> "🔥"
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .cornerRadius(20.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = GlanceModifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = emoji,
                            style = TextStyle(fontSize = 32.sp),
                        )
                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            text = "$done / $total",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary,
                            ),
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "выполнено сегодня",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = onSurfaceVariant,
                            ),
                        )
                        if (total > 0) {
                            Spacer(GlanceModifier.height(10.dp))
                            // Простая текстовая прогресс-полоса
                            val filled = if (total > 0) (done * 10 / total) else 0
                            Text(
                                text = "▓".repeat(filled) + "░".repeat(10 - filled),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = if (filled > 0) primary else onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodaySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodaySummaryWidget()
}
