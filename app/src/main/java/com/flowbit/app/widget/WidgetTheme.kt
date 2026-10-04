package com.flowbit.app.widget

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as GlanceColorProviders
import com.flowbit.app.presentation.settings.SettingsViewModel
import com.flowbit.app.presentation.theme.ObsidianDarkColorScheme
import com.flowbit.app.presentation.theme.ObsidianLightColorScheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val ObsidianGlanceColors: ColorProviders = GlanceColorProviders(
    light = ObsidianLightColorScheme,
    dark  = ObsidianDarkColorScheme,
)

suspend fun buildGlanceColors(context: Context): ColorProviders {
    val ep = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
    val accentHex = ep.dataStore().data
        .map { it[SettingsViewModel.ACCENT_COLOR_KEY] }
        .first()

    val lightBase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicLightColorScheme(context) else ObsidianLightColorScheme
    val darkBase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicDarkColorScheme(context) else ObsidianDarkColorScheme

    return if (!accentHex.isNullOrBlank()) {
        runCatching {
            val accent = Color(android.graphics.Color.parseColor(accentHex))
            val lum = with(accent) { 0.2126f * red + 0.7152f * green + 0.0722f * blue }
            val onAccent = if (lum > 0.45f) Color(0xFF1C1C1C) else Color.White
            GlanceColorProviders(
                light = lightBase.copy(
                    primary = accent,
                    onPrimary = onAccent,
                    primaryContainer = accent.copy(alpha = 0.18f),
                    onPrimaryContainer = accent.copy(alpha = 0.8f),
                ),
                dark = darkBase.copy(
                    primary = accent,
                    onPrimary = onAccent,
                    primaryContainer = accent.copy(alpha = 0.25f),
                    onPrimaryContainer = accent.copy(alpha = 0.9f),
                ),
            )
        }.getOrDefault(GlanceColorProviders(light = lightBase, dark = darkBase))
    } else {
        GlanceColorProviders(light = lightBase, dark = darkBase)
    }
}
