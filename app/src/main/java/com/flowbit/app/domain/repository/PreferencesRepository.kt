package com.flowbit.app.domain.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flowbit.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val isCompactMode: Flow<Boolean>
    val formStyle: Flow<String>
    val analyticsStyle: Flow<String>
    val accentColorHex: Flow<String?>
    val onboardingShown: Flow<Boolean>
    val showInWidgetByDefault: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCompactMode(enabled: Boolean)
    suspend fun setFormStyle(style: String)
    suspend fun setAnalyticsStyle(style: String)
    suspend fun setAccentColor(hex: String)
    suspend fun clearAccentColor()
    suspend fun setOnboardingShown()
    suspend fun setShowInWidgetByDefault(enabled: Boolean)

    companion object {
        val THEME_MODE_KEY      = stringPreferencesKey("theme_mode")
        val COMPACT_MODE_KEY    = booleanPreferencesKey("compact_mode")
        val FORM_STYLE_KEY      = stringPreferencesKey("form_style")
        val ANALYTICS_STYLE_KEY = stringPreferencesKey("analytics_style")
        val ACCENT_COLOR_KEY    = stringPreferencesKey("accent_color")
        val ONBOARDING_SHOWN_KEY = booleanPreferencesKey("onboarding_shown")
        val WIDGET_BY_DEFAULT_KEY = booleanPreferencesKey("show_in_widget_by_default")

        const val DEFAULT_ACCENT_HEX = "#2DD4BF"

        // Явный выбор цвета из обоев. Пустое значение означает стартовый акцент, а не Material You
        const val WALLPAPER_ACCENT = "wallpaper"

        fun resolveAccentHex(stored: String?): String? = when (stored) {
            null -> DEFAULT_ACCENT_HEX
            WALLPAPER_ACCENT -> null
            else -> stored
        }
    }
}
