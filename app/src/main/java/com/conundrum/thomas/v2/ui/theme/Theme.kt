package com.conundrum.thomas.v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.conundrum.thomas.v2.ThomasColorProfile
import com.conundrum.thomas.v2.ThomasTextSize

private fun dark(bg: Long, surface: Long, primary: Long, onBg: Long, variant: Long) = darkColorScheme(
    primary = C(primary), onPrimary = C(0xFF101418), primaryContainer = C(variant), onPrimaryContainer = C(onBg),
    secondary = C(onBg), onSecondary = C(0xFF101418), secondaryContainer = C(surface), onSecondaryContainer = C(onBg),
    tertiary = C(primary), onTertiary = C(0xFF101418), tertiaryContainer = C(variant), onTertiaryContainer = C(onBg),
    background = C(bg), onBackground = C(onBg), surface = C(surface), onSurface = C(onBg),
    surfaceVariant = C(variant), onSurfaceVariant = C(onBg), outline = C(onBg), error = C(0xFFFFB4AB),
)
private fun light(bg: Long, surface: Long, primary: Long, ink: Long, variant: Long) = lightColorScheme(
    primary = C(primary), onPrimary = C(0xFFFFFFFF), primaryContainer = C(variant), onPrimaryContainer = C(ink),
    secondary = C(ink), onSecondary = C(0xFFFFFFFF), secondaryContainer = C(surface), onSecondaryContainer = C(ink),
    tertiary = C(primary), onTertiary = C(0xFFFFFFFF), tertiaryContainer = C(variant), onTertiaryContainer = C(ink),
    background = C(bg), onBackground = C(ink), surface = C(surface), onSurface = C(ink),
    surfaceVariant = C(variant), onSurfaceVariant = C(ink), outline = C(ink), error = C(0xFFB3261E),
)
private fun C(value: Long) = androidx.compose.ui.graphics.Color(value)

@Composable
fun ConundrumThomasV2Theme(
    profile: ThomasColorProfile = ThomasColorProfile.THOMAS_DARK,
    textSize: ThomasTextSize = ThomasTextSize.STANDARD,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val colors = when (profile) {
        ThomasColorProfile.THOMAS_DARK -> dark(0xFF121820, 0xFF1C2733, 0xFF9DCCFF, 0xFFF4F0E8, 0xFF293849)
        ThomasColorProfile.MIDNIGHT -> dark(0xFF091523, 0xFF102235, 0xFF9DCCFF, 0xFFE5F2FF, 0xFF1B4160)
        ThomasColorProfile.QUIET -> dark(0xFF201D1A, 0xFF2B2824, 0xFFE5C588, 0xFFF2E8D8, 0xFF5D4A2F)
        ThomasColorProfile.PAPER -> light(0xFFFFF9EE, 0xFFF2EBDD, 0xFF1E4C72, 0xFF1B1A18, 0xFFC8E3FF)
        ThomasColorProfile.HIGH_CONTRAST_DARK -> dark(0xFF000000, 0xFF000000, 0xFFFFFF00, 0xFFFFFFFF, 0xFF1A1A1A)
        ThomasColorProfile.HIGH_CONTRAST_LIGHT -> light(0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 0xFFF0F0F0)
    }
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * textSize.scale)) {
        MaterialTheme(colorScheme = colors, typography = Typography, content = content)
    }
}
