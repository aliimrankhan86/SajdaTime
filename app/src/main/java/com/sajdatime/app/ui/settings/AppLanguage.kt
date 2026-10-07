package com.sajdatime.app.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * The languages the in-app picker offers, through Android's per-app language support
 * (API 33+; `res/xml/locales_config.xml` declares the same list to the system).
 *
 * **Offering a language is not the same as having it.** CLAUDE.md forbids machine translation:
 * a language ships only after a native speaker has reviewed it. So every entry is listed, but
 * [isAvailable] is true only when a `values-<tag>/` folder really resolves `app_language_tag`
 * to that language. Until then the row is shown disabled ("awaiting review") and choosing it is
 * impossible, which also means the app can never be put into a right-to-left layout while still
 * written in English (AppLocale.kt). The day a reviewed `values-ur/` lands, Urdu becomes
 * selectable with no code change here.
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    // Names are written in their own language and script on purpose, so a user who cannot
    // read the current language can still find theirs. Not translatable.
    ENGLISH("en", "English"),
    URDU("ur", "اردو"),
    TURKISH("tr", "Türkçe"),
    INDONESIAN("id", "Bahasa Indonesia"),
    ;

    /** True when a reviewed translation for this language is compiled into the app. */
    fun isAvailable(context: Context): Boolean {
        if (this == ENGLISH) return true
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
        val resolved = context.createConfigurationContext(config).getString(com.sajdatime.core.R.string.app_language_tag)
        return Locale.forLanguageTag(resolved).language == Locale.forLanguageTag(tag).language
    }

    companion object {
        /** Per-app language needs Android 13; below that the system has nothing to hand us. */
        val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        /** What the user chose in the system or in the picker; English when nothing is set. */
        fun current(context: Context): AppLanguage {
            if (!supported) return ENGLISH
            val chosen = context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales?.takeIf { !it.isEmpty }?.get(0)?.language
            return entries.firstOrNull { it.tag == chosen } ?: ENGLISH
        }

        fun apply(context: Context, language: AppLanguage) {
            if (!supported) return
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(language.tag)
        }
    }
}
