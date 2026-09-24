package com.conundrum.thomas.v2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.conundrum.thomas.v2.R

val IbmPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold),
)

val IbmPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)

private val MaterialDefaults = Typography()

/** Thomas uses IBM Plex Sans throughout; Plex Mono is reserved for technical identity treatments. */
val Typography = MaterialDefaults.copy(
    displayLarge = MaterialDefaults.displayLarge.copy(fontFamily = IbmPlexSans),
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = IbmPlexSans),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = IbmPlexSans),
    headlineLarge = MaterialDefaults.headlineLarge.copy(fontFamily = IbmPlexSans),
    headlineMedium = MaterialDefaults.headlineMedium.copy(fontFamily = IbmPlexSans),
    headlineSmall = MaterialDefaults.headlineSmall.copy(fontFamily = IbmPlexSans),
    titleLarge = MaterialDefaults.titleLarge.copy(fontFamily = IbmPlexSans),
    titleMedium = MaterialDefaults.titleMedium.copy(fontFamily = IbmPlexSans),
    titleSmall = MaterialDefaults.titleSmall.copy(fontFamily = IbmPlexSans),
    bodyLarge = MaterialDefaults.bodyLarge.copy(fontFamily = IbmPlexSans),
    bodyMedium = MaterialDefaults.bodyMedium.copy(fontFamily = IbmPlexSans),
    bodySmall = MaterialDefaults.bodySmall.copy(fontFamily = IbmPlexSans),
    labelLarge = MaterialDefaults.labelLarge.copy(fontFamily = IbmPlexSans),
    labelMedium = MaterialDefaults.labelMedium.copy(fontFamily = IbmPlexSans),
    labelSmall = MaterialDefaults.labelSmall.copy(fontFamily = IbmPlexSans),
)
