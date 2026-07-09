package com.flowbit.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.flowbit.app.data.database.entity.HabitEntity
import com.flowbit.app.presentation.theme.FlowbitTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SingleHabitWidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            FlowbitTheme {
                val scope = rememberCoroutineScope()
                var habits by remember { mutableStateOf<List<HabitEntity>>(emptyList()) }

                LaunchedEffect(Unit) {
                    val ep = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
                    habits = ep.database().habitDao().getActiveHabits().first()
                }

                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(title = { Text("Выбрать привычку для виджета") })
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(habits) { habit ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            val glanceId = GlanceAppWidgetManager(applicationContext)
                                                .getGlanceIdBy(appWidgetId)
                                            updateAppWidgetState(
                                                context = applicationContext,
                                                definition = PreferencesGlanceStateDefinition,
                                                glanceId = glanceId,
                                            ) { prefs ->
                                                prefs.toMutablePreferences().apply {
                                                    this[SingleHabitWidget.PINNED_HABIT_KEY] = habit.id
                                                }
                                            }
                                            SingleHabitWidget().update(applicationContext, glanceId)
                                            setResult(RESULT_OK, Intent().apply {
                                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                            })
                                            finish()
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = habit.emoji,
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
