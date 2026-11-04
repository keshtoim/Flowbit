package com.flowbit.app.widget

import android.content.Context
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as GlanceColorProviders
import com.flowbit.app.domain.repository.PreferencesRepository
import com.flowbit.app.presentation.theme.flowbitColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

// Та же схема, что в приложении: тема из настроек приложения, а не только системная, и тот же акцент
suspend fun buildGlanceColors(context: Context): ColorProviders {
    val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
    val prefs = ep.dataStore().data.first()
    val accentHex = PreferencesRepository.resolveAccentHex(prefs[PreferencesRepository.ACCENT_COLOR_KEY])
    val light = flowbitColorScheme(context, darkTheme = false, accentHex = accentHex)
    val dark = flowbitColorScheme(context, darkTheme = true, accentHex = accentHex)
    return when (prefs[PreferencesRepository.THEME_MODE_KEY]) {
        "light" -> GlanceColorProviders(light = light, dark = light)
        "dark" -> GlanceColorProviders(light = dark, dark = dark)
        else -> GlanceColorProviders(light = light, dark = dark)
    }
}
