package com.flowbit.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val ObsidianDarkColorScheme = darkColorScheme(
    primary                = ObsidianCoral,
    onPrimary              = ObsidianOnError,           // white text on coral
    primaryContainer       = Color(0xFF5E1A0D),
    onPrimaryContainer     = Color(0xFFFFD0C5),

    secondary              = Color(0xFF9B7FF5),         // фиолетовый — для мульти-счётчика
    onSecondary            = Color(0xFFFFFFFF),
    secondaryContainer     = Color(0xFF2E1A6E),
    onSecondaryContainer   = Color(0xFFE5DAFF),

    tertiary               = ObsidianTertiary,          // зелёный — "чисто" / streak-safe
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
    content: @Composable () -> Unit,
) {
    // Dynamic Color (Material You) намеренно отключён — используем фиксированную Obsidian-палитру
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else ObsidianLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
