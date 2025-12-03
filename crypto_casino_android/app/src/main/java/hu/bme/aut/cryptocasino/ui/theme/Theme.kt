package hu.bme.aut.cryptocasino.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DraculaDarkColorScheme =
    darkColorScheme(
        primary = Primary,
        onPrimary = Background,
        primaryContainer = PrimaryDark,
        onPrimaryContainer = PrimaryLight,
        secondary = Secondary,
        onSecondary = Background,
        secondaryContainer = SecondaryDark,
        onSecondaryContainer = SecondaryLight,
        tertiary = Tertiary,
        onTertiary = Background,
        tertiaryContainer = TertiaryDark,
        onTertiaryContainer = TertiaryLight,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        onSurface = OnSurface,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant,
        error = Error,
        onError = Background,
        errorContainer = Error,
        onErrorContainer = OnBackground,
        outline = OnSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = OnSurfaceVariant.copy(alpha = 0.3f),
        scrim = Background.copy(alpha = 0.5f),
        inverseSurface = OnSurface,
        inverseOnSurface = Surface,
        inversePrimary = PrimaryDark,
        surfaceTint = Primary,
    )

private val DraculaLightColorScheme =
    lightColorScheme(
        primary = PrimaryLightMode,
        onPrimary = BackgroundLight,
        primaryContainer = PrimaryLightLightMode,
        onPrimaryContainer = PrimaryDarkLightMode,
        secondary = SecondaryLightMode,
        onSecondary = BackgroundLight,
        secondaryContainer = SecondaryLightLightMode,
        onSecondaryContainer = SecondaryDarkLightMode,
        tertiary = TertiaryLightMode,
        onTertiary = BackgroundLight,
        tertiaryContainer = TertiaryLightLightMode,
        onTertiaryContainer = TertiaryDarkLightMode,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        error = ErrorLight,
        onError = BackgroundLight,
        errorContainer = ErrorLight.copy(alpha = 0.1f),
        onErrorContainer = ErrorLight,
        outline = OnSurfaceVariantLight.copy(alpha = 0.5f),
        outlineVariant = OnSurfaceVariantLight.copy(alpha = 0.3f),
        scrim = OnBackgroundLight.copy(alpha = 0.5f),
        inverseSurface = OnSurfaceLight,
        inverseOnSurface = SurfaceLight,
        inversePrimary = PrimaryLightLightMode,
        surfaceTint = PrimaryLightMode,
    )

@Composable
fun CryptocasinoandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        if (darkTheme) {
            DraculaDarkColorScheme
        } else {
            DraculaLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

object ThemeColors {
    val draculaGold: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) DraculaGold else DraculaGoldLight

    val draculaReelBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) DraculaReelBackground else DraculaReelBackgroundLight

    val draculaCardBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) DraculaCardBackground else DraculaCardBackgroundLight

    val purple: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Purple else PurpleLight

    val amber: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Amber else AmberLight

    val bet: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Bet else BetLight

    val win: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Win else WinLight

    val success: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Success else SuccessLight

    val error: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Error else ErrorLight

    val warning: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Warning else WarningLight

    val info: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Info else InfoLight
}
