package com.flowbit.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

internal val ObsidianDarkColorScheme = darkColorScheme(
    primary                = ObsidianCoral,
    onPrimary              = ObsidianOnError,
    primaryContainer       = Color(0xFF5E1A0D),
    onPrimaryContainer     = Color(0xFFFFD0C5),

    secondary              = Color(0xFF9B7FF5),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFF2E1A6E),
    onSecondaryContainer   = Color(0xFFE5DAFF),

    tertiary               = ObsidianTertiary,
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = ObsidianTertiaryContainer,
    onTertiaryContainer    = Color(0xFFB7F5DA),

    error                  = ObsidianError,
    onError                = ObsidianOnError,
    errorContainer         = ObsidianErrorContainer,
    onErrorContainer       = ObsidianOnErrorContainer,

    background             = ObsidianBg,
    onBackground           = ObsidianOnBg,

    surface                = ObsidianSurface,
    onSurface              = ObsidianOnBg,
    surfaceVariant         = ObsidianSurface2,
    onSurfaceVariant       = ObsidianMuted,
    outline                = ObsidianDivider,
)

internal val ObsidianLightColorScheme = lightColorScheme(
    primary                = LightPrimary,
    onPrimary              = LightOnPrimary,
    primaryContainer       = LightPrimaryContainer,
    onPrimaryContainer     = LightOnPrimaryContainer,

    secondary              = Color(0xFF7850FF),
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFFEAE0FF),
    onSecondaryContainer   = Color(0xFF22005D),

    tertiary               = Color(0xFF4CAF8A),
    onTertiary             = Color(0xFFFFFFFF),
    tertiaryContainer      = Color(0xFFCCF5E5),
    onTertiaryContainer    = Color(0xFF003825),

    error                  = Color(0xFFBA1A1A),
    onError                = Color(0xFFFFFFFF),
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),

    background             = LightBg,
    onBackground           = LightOnBg,
    surface                = LightSurface,
    onSurface              = LightOnSurface,
    surfaceVariant         = LightSurfaceVariant,
    onSurfaceVariant       = LightOnSurfaceVariant,
    outline                = LightOutline,
)

@Composable
fun FlowbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customAccentHex: String? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Базовая схема: Material You на API 31+, иначе Obsidian
    val baseScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) ObsidianDarkColorScheme else ObsidianLightColorScheme
    }

    // Если задан кастомный цвет — переопределяем primary поверх базовой схемы
    val colorScheme = if (!customAccentHex.isNullOrBlank()) {
        runCatching {
            val accent = Color(android.graphics.Color.parseColor(customAccentHex))
            val lum = with(accent) { 0.2126f * red + 0.7152f * green + 0.0722f * blue }
            val onAccent = if (lum > 0.45f) Color(0xFF1C1C1C) else Color.White
            baseScheme.copy(
                primary = accent,
                onPrimary = onAccent,
                primaryContainer = accent.copy(alpha = if (darkTheme) 0.25f else 0.18f),
                onPrimaryContainer = if (darkTheme) accent.copy(alpha = 0.9f) else accent.copy(alpha = 0.8f),
            )
        }.getOrDefault(baseScheme)
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
