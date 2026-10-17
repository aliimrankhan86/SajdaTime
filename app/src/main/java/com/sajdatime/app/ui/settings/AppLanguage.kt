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
 * **Offering a language is not the same as having it on the phone.** [isAvailable] is true only
 * when a `values-<tag>/` folder really resolves `app_language_tag` to that language on this
 * device; otherwise the row is shown disabled and choosing it is impossible, which also means the
 * app can never be put into a right-to-left layout while still written in English (AppLocale.kt).
 * Since 1.3.2 every language is packaged into the app itself (`bundle.language.enableSplit =
 * false` in app/build.gradle.kts), so on a Play install this is a safety net, not a gate. Before
 * that, Play installed only the phone's own language and every other row was greyed out.
 *
 * Urdu, Turkish and Indonesian are Claude's translation (8 Oct 2026). On 9 Oct 2026 the owner
 * decided they need no native speaker review: SajdaTime is a free utility he makes himself, and
 * the translator's own checking is the standard. The app must not tell users otherwise.
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    // Names are written in their own language and script on purpose, so a user who cannot
    // read the current language can still find theirs. Not translatable.
    ENGLISH("en", "English"),
    URDU("ur", "اردو"),
    TURKISH("tr", "Türkçe"),
    INDONESIAN("id", "Bahasa Indonesia"),
    // Added 17 Oct 2026 after a 1 star review asked for it ("إضافة اللغة العربية"). Claude's
    // translation, checked by Claude, as the owner decided for every language on 9 Oct 2026.
    // Choosing it turns the app right to left with Arabic-Indic digits through AppLocale.
    ARABIC("ar", "العربية"),
    ;

    /** True when this language's words are installed on this phone. */
    fun isAvailable(context: Context): Boolean {
        if (this == ENGLISH) return true
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
        val resolved = context.createConfigurationContext(config).getString(com.sajdatime.core.R.string.app_language_tag)
        return normalise(Locale.forLanguageTag(resolved).language) == normalise(Locale.forLanguageTag(tag).language)
    }

    companion object {
        /** Per-app language needs Android 13; below that the system has nothing to hand us. */
        val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        /** What the user chose in the system or in the picker; English when nothing is set. */
        fun current(context: Context): AppLanguage {
            if (!supported) return ENGLISH
            val chosen = context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales?.takeIf { !it.isEmpty }?.get(0)
            return fromLocale(chosen)
        }

        /**
         * The picker row for [locale], English when none matches.
         *
         * Compares by normalised language code, not by `tag == locale.language`. Android's
         * `Locale.getLanguage()` returns the legacy code "in" for Indonesian (and "iw" for
         * Hebrew), so the plain comparison 1.3.1 shipped could never match "id": once a user
         * chose Indonesian, Settings would show English ticked while the app was in Indonesian.
         * Found by reading the code on 10 Oct 2026, not seen on a phone (Indonesian could not be
         * chosen at all from a Play install until the language split fix).
         */
        fun fromLocale(locale: Locale?): AppLanguage {
            val chosen = locale?.language?.let(::normalise) ?: return ENGLISH
            return entries.firstOrNull { normalise(it.tag) == chosen } ?: ENGLISH
        }

        /** Maps the legacy ISO 639 codes Android still hands out to the modern ones. */
        internal fun normalise(language: String): String = when (val code = language.lowercase(Locale.ROOT)) {
            "in" -> "id"
            "iw" -> "he"
            "ji" -> "yi"
            else -> code
        }

        fun apply(context: Context, language: AppLanguage) {
            if (!supported) return
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(language.tag)
        }
    }
}
