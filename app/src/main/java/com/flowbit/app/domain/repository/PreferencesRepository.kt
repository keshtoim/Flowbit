package com.flowbit.app.domain.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flowbit.app.domain.model.ListDensity
import com.flowbit.app.domain.model.ListViewSettings
import com.flowbit.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val listViewSettings: Flow<ListViewSettings>
    val formStyle: Flow<String>
    val analyticsStyle: Flow<String>
    val accentColorHex: Flow<String?>
    val onboardingShown: Flow<Boolean>
    val showInWidgetByDefault: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setListDensity(density: ListDensity)
    suspend fun setShowHistory(enabled: Boolean)
    suspend fun setCollapseCompleted(enabled: Boolean)
    suspend fun setShowTagFilter(enabled: Boolean)
    suspend fun setBigSummary(enabled: Boolean)
    suspend fun setFormStyle(style: String)
    suspend fun setAnalyticsStyle(style: String)
    suspend fun setAccentColor(hex: String)
    suspend fun clearAccentColor()
    suspend fun setOnboardingShown()
    suspend fun setShowInWidgetByDefault(enabled: Boolean)

    companion object {
        val THEME_MODE_KEY      = stringPreferencesKey("theme_mode")
        val COMPACT_MODE_KEY    = booleanPreferencesKey("compact_mode")
        val LIST_DENSITY_KEY    = stringPreferencesKey("list_density")
        val SHOW_HISTORY_KEY    = booleanPreferencesKey("list_show_history")
        val COLLAPSE_DONE_KEY   = booleanPreferencesKey("list_collapse_completed")
        val TAG_FILTER_KEY      = booleanPreferencesKey("list_tag_filter")
        val BIG_SUMMARY_KEY     = booleanPreferencesKey("list_big_summary")
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
