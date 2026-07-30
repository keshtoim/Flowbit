package com.flowbit.app.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.rememberNavController
import com.flowbit.app.presentation.navigation.FlowbitNavGraph
import com.flowbit.app.presentation.settings.SettingsViewModel.Companion.THEME_MODE_KEY
import com.flowbit.app.presentation.theme.FlowbitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val habitIdFromShortcut = if (intent?.action == ShortcutHelper.ACTION_OPEN_HABIT)
            intent.getLongExtra(ShortcutHelper.EXTRA_HABIT_ID, -1L).takeIf { it > 0 }
        else null

        setContent {
            val themeModePref by dataStore.data
                .map { it[THEME_MODE_KEY] ?: "system" }
                .collectAsState(initial = "system")
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeModePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemDark
            }

            FlowbitTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                FlowbitNavGraph(
                    navController = navController,
                    initialHabitId = habitIdFromShortcut,
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
