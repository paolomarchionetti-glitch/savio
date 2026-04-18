package com.paolomarchionetti.savio.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SavioLightColorScheme = lightColorScheme(
    primary             = SavioGreen700,
    onPrimary           = White,
    primaryContainer    = SavioGreen100,
    onPrimaryContainer  = SavioGreen900,

    secondary           = SavioAmber600,
    onSecondary         = White,
    secondaryContainer  = SavioAmber100,
    onSecondaryContainer = SavioGreen900,

    background          = SavioNeutral050,
    onBackground        = SavioNeutral950,

    surface             = White,
    onSurface           = SavioNeutral950,
    surfaceVariant      = SavioNeutral100,
    onSurfaceVariant    = SavioNeutral600,

    outline             = SavioNeutral200,
    outlineVariant      = SavioNeutral100,

    error               = SavioError,
    onError             = White,
)

private val SavioDarkColorScheme = darkColorScheme(
    primary             = SavioGreen500,
    onPrimary           = SavioGreen900,
    primaryContainer    = SavioGreen800,
    onPrimaryContainer  = SavioGreen100,

    secondary           = SavioAmber500,
    onSecondary         = SavioGreen900,
    secondaryContainer  = SavioAmber600,
    onSecondaryContainer = White,

    background          = SavioNeutral950,
    onBackground        = SavioNeutral100,

    surface             = SavioNeutral800,
    onSurface           = SavioNeutral100,
    surfaceVariant      = SavioNeutral800,
    onSurfaceVariant    = SavioNeutral400,

    outline             = SavioNeutral600,
    outlineVariant      = SavioNeutral800,

    error               = SavioError,
    onError             = White,
)

/**
 * Tema principale Savio.
 * Dynamic color abilitato su Android 12+ (Material You) ma con fallback
 * ai colori brand definiti sopra per coerenza visiva.
 */
@Composable
fun SavioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // false: mantieni colori brand Savio
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SavioDarkColorScheme
        else      -> SavioLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SavioTypography,
        content     = content
    )
}
