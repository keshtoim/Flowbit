package com.flowbit.app.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.flowbit.app.presentation.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

// Минималистичный виджет для экрана блокировки (Android 13+ / keyguard).
// Показывает: эмодзи состояния + "X/N" выполнено сегодня.
class LockScreenWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val db = ep.database()
        val today = LocalDate.now().toString()

        val habits = db.habitDao().getActiveHabits().first()
        val entries = db.habitDao().getEntriesForDate(today).first()
            .associateBy { it.habitId }

        val total = habits.size
        val done = habits.count { h ->
            val e = entries[h.id]
            e?.isSkipped != true && (e?.completedCount ?: 0) >= h.targetCount
        }

        val emoji = when {
            total == 0 -> "🌱"
            done == total -> "🎉"
            done.toFloat() / total.coerceAtLeast(1) >= 0.5f -> "💪"
            else -> "🔥"
        }

        val glanceColors = buildGlanceColors(context)

        provideContent {
            GlanceTheme(colors = glanceColors) {
                val bg = GlanceTheme.colors.surface
                val primary = GlanceTheme.colors.primary
                val onSurface = GlanceTheme.colors.onSurface

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .cornerRadius(16.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = emoji, style = TextStyle(fontSize = 24.sp))
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "$done/$total",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary,
                            ),
                        )
                        Text(
                            text = "сегодня",
                            style = TextStyle(fontSize = 10.sp, color = onSurface),
                        )
                    }
                }
            }
        }
    }
}

class LockScreenWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LockScreenWidget()
}
