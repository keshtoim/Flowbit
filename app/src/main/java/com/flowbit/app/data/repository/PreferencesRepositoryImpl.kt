package com.flowbit.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.flowbit.app.domain.model.ListDensity
import com.flowbit.app.domain.model.ListViewSettings
import com.flowbit.app.domain.model.ThemeMode
import com.flowbit.app.domain.repository.PreferencesRepository
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.ACCENT_COLOR_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.ANALYTICS_STYLE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.BIG_SUMMARY_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.COLLAPSE_DONE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.COMPACT_MODE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.LIST_DENSITY_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.SHOW_HISTORY_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.TAG_FILTER_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.FORM_STYLE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.ONBOARDING_SHOWN_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.THEME_MODE_KEY
import com.flowbit.app.domain.repository.PreferencesRepository.Companion.WIDGET_BY_DEFAULT_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    override val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[THEME_MODE_KEY]) {
            "light" -> ThemeMode.LIGHT
            "dark"  -> ThemeMode.DARK
            else    -> ThemeMode.SYSTEM
        }
    }

    override val listViewSettings: Flow<ListViewSettings> = dataStore.data.map { prefs ->
        ListViewSettings(
            // Старый «компактный режим» переносится в плотность «Мини»
            density = prefs[LIST_DENSITY_KEY]?.let { runCatching { ListDensity.valueOf(it) }.getOrNull() }
                ?: if (prefs[COMPACT_MODE_KEY] == true) ListDensity.MINI else ListDensity.ROWS,
            showHistory = prefs[SHOW_HISTORY_KEY] ?: true,
            collapseCompleted = prefs[COLLAPSE_DONE_KEY] ?: true,
            showTagFilter = prefs[TAG_FILTER_KEY] ?: true,
            bigSummary = prefs[BIG_SUMMARY_KEY] ?: false,
        )
    }

    override val formStyle: Flow<String> =
        dataStore.data.map { prefs -> prefs[FORM_STYLE_KEY] ?: "A" }

    override val analyticsStyle: Flow<String> =
        dataStore.data.map { prefs -> prefs[ANALYTICS_STYLE_KEY] ?: "A" }

    override val accentColorHex: Flow<String?> =
        dataStore.data.map { prefs -> PreferencesRepository.resolveAccentHex(prefs[ACCENT_COLOR_KEY]) }

    override val onboardingShown: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[ONBOARDING_SHOWN_KEY] ?: false }

    override val showInWidgetByDefault: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[WIDGET_BY_DEFAULT_KEY] ?: false }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = when (mode) {
                ThemeMode.SYSTEM -> "system"
                ThemeMode.LIGHT  -> "light"
                ThemeMode.DARK   -> "dark"
            }
        }
    }

    override suspend fun setListDensity(density: ListDensity) {
        dataStore.edit { it[LIST_DENSITY_KEY] = density.name }
    }

    override suspend fun setShowHistory(enabled: Boolean) {
        dataStore.edit { it[SHOW_HISTORY_KEY] = enabled }
    }

    override suspend fun setCollapseCompleted(enabled: Boolean) {
        dataStore.edit { it[COLLAPSE_DONE_KEY] = enabled }
    }

    override suspend fun setShowTagFilter(enabled: Boolean) {
        dataStore.edit { it[TAG_FILTER_KEY] = enabled }
    }

    override suspend fun setBigSummary(enabled: Boolean) {
        dataStore.edit { it[BIG_SUMMARY_KEY] = enabled }
    }

    override suspend fun setFormStyle(style: String) {
        dataStore.edit { it[FORM_STYLE_KEY] = style }
    }

    override suspend fun setAnalyticsStyle(style: String) {
        dataStore.edit { it[ANALYTICS_STYLE_KEY] = style }
    }

    override suspend fun setAccentColor(hex: String) {
        dataStore.edit { it[ACCENT_COLOR_KEY] = hex }
    }

    override suspend fun clearAccentColor() {
        dataStore.edit { it[ACCENT_COLOR_KEY] = PreferencesRepository.WALLPAPER_ACCENT }
    }

    override suspend fun setOnboardingShown() {
        dataStore.edit { it[ONBOARDING_SHOWN_KEY] = true }
    }

    override suspend fun setShowInWidgetByDefault(enabled: Boolean) {
        dataStore.edit { it[WIDGET_BY_DEFAULT_KEY] = enabled }
    }
}
