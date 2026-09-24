package com.conundrum.thomas.v2

import android.content.Context
import android.net.Uri
import java.io.File
import com.conundrum.thomas.v2.platform.speech.ThomasSpeechRate
import com.conundrum.thomas.v2.platform.speech.ThomasVoiceProfile

/** Presentation-only preferences. They are deliberately outside Thomas history. */
data class ThomasVoicePreferences(
    val autoSpeak: Boolean = true,
    val profile: ThomasVoiceProfile = ThomasVoiceProfile.NEUTRAL,
    val rate: ThomasSpeechRate = ThomasSpeechRate.NORMAL,
    val stopWhenMicrophoneStarts: Boolean = true,
    val colorProfile: ThomasColorProfile = ThomasColorProfile.THOMAS_DARK,
    val textSize: ThomasTextSize = ThomasTextSize.STANDARD,
    val reduceMotion: Boolean = false,
    /** App-private presentation file, never a source, attachment, or model input. */
    val backgroundPath: String? = null,
    val backgroundDim: BackgroundDim = BackgroundDim.MEDIUM,
    val backgroundBlur: Boolean = false,
    val backgroundCrop: BackgroundCrop = BackgroundCrop.CROP,
)

enum class ThomasColorProfile { THOMAS_DARK, MIDNIGHT, QUIET, PAPER, HIGH_CONTRAST_DARK, HIGH_CONTRAST_LIGHT }
enum class ThomasTextSize(val scale: Float) { SMALL(0.90f), STANDARD(1f), LARGE(1.15f), EXTRA_LARGE(1.30f) }
enum class BackgroundDim(val alpha: Float) { LIGHT(0.56f), MEDIUM(0.68f), STRONG(0.78f) }
enum class BackgroundCrop { FIT, CROP }

class AndroidThomasVoicePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("thomas_voice_presentation", Context.MODE_PRIVATE)

    fun read() = ThomasVoicePreferences(
        autoSpeak = preferences.getBoolean("auto_speak", true),
        profile = enum("profile", ThomasVoiceProfile.NEUTRAL),
        rate = enum("rate", ThomasSpeechRate.NORMAL),
        stopWhenMicrophoneStarts = preferences.getBoolean("stop_on_mic", true),
        colorProfile = enum("color_profile", ThomasColorProfile.THOMAS_DARK),
        textSize = enum("text_size", ThomasTextSize.STANDARD),
        reduceMotion = preferences.getBoolean("reduce_motion", false),
        backgroundPath = preferences.getString("background_path", null)?.takeIf { File(it).isFile },
        backgroundDim = enum("background_dim", BackgroundDim.MEDIUM),
        backgroundBlur = preferences.getBoolean("background_blur", false),
        backgroundCrop = enum("background_crop", BackgroundCrop.CROP),
    )

    fun write(value: ThomasVoicePreferences) {
        preferences.edit()
            .putBoolean("auto_speak", value.autoSpeak)
            .putString("profile", value.profile.name)
            .putString("rate", value.rate.name)
            .putBoolean("stop_on_mic", value.stopWhenMicrophoneStarts)
            .putString("color_profile", value.colorProfile.name)
            .putString("text_size", value.textSize.name)
            .putBoolean("reduce_motion", value.reduceMotion)
            .putString("background_path", value.backgroundPath)
            .putString("background_dim", value.backgroundDim.name)
            .putBoolean("background_blur", value.backgroundBlur)
            .putString("background_crop", value.backgroundCrop.name)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enum(key: String, fallback: T): T =
        preferences.getString(key, fallback.name)?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback
}

/** Owns a single bounded app-private image. The original URI and metadata are not retained. */
class ThomasBackgroundCustody(context: Context) {
    private val applicationContext = context.applicationContext
    private val directory = File(applicationContext.filesDir, "presentation-background").apply { mkdirs() }
    private val target = File(directory, "background-image")

    fun import(uri: Uri): String? = runCatching {
        val temporary = File(directory, "background-image.tmp")
        try {
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    val copied = input.copyTo(output)
                    require(copied in 1..(16 * 1024 * 1024)) { "Background image must be 16 MB or smaller" }
                }
            } ?: return null
            if (target.exists()) target.delete()
            require(temporary.renameTo(target)) { "Could not preserve selected background" }
            target.absolutePath
        } finally {
            if (temporary.exists() && temporary.absolutePath != target.absolutePath) temporary.delete()
        }
    }.getOrNull()

    fun remove() {
        target.delete()
        File(directory, "background-image.tmp").delete()
    }
}
