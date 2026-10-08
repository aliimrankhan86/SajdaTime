package com.sajdatime.app.ui.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.sajdatime.core.AppLocale
import java.util.Locale

/**
 * The languages the in-app picker offers.
 *
 * **The default is not a language, it is "follow the phone".** Most people never open this
 * picker, and for them the app already speaks the phone's language when it has words in it
 * ([AppLocale] reads the language back from the resources). So [chosen] is null until the
 * user picks something, and the picker shows "Phone language" ticked, not "English". Showing
 * English there was wrong for a Turkish phone running the Turkish app.
 *
 * **Two routes, one outcome.** From Android 13 the system keeps a per-app language
 * (`LocaleManager`, declared to it by `res/xml/locales_config.xml`). Below 13 there is nothing
 * to hand us, and a great many phones in this app's audience are below 13 and set to English
 * while their owner reads Urdu, so the choice is stored by [AppLocale.setOverride] and applied
 * when each context is wrapped. Either way the app's language is still the language of the
 * resources it resolves to, never the stored tag itself.
 *
 * **Offering a language is not the same as having it.** [isAvailable] is true only when a
 * `values-<tag>/` folder really resolves `app_language_tag` to that language, so a language
 * can be listed without ever putting the app in a layout direction its words do not match.
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    // Names are written in their own language and script on purpose, so a user who cannot
    // read the current language can still find theirs. Not translatable.
    ENGLISH("en", "English"),
    URDU("ur", "اردو"),
    TURKISH("tr", "Türkçe"),
    INDONESIAN("id", "Bahasa Indonesia"),
    ;

    /** True when a translation for this language is compiled into the app. */
    fun isAvailable(context: Context): Boolean {
        if (this == ENGLISH) return true
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
        val resolved = context.createConfigurationContext(config).getString(com.sajdatime.core.R.string.app_language_tag)
        return sameLanguage(resolved, tag)
    }

    companion object {
        /** Kept for callers that ask; every Android version this app supports now has a route. */
        val supported: Boolean get() = true

        /** What the user picked, or null when they have not and the app follows the phone. */
        fun chosen(context: Context): AppLanguage? {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)
                    ?.applicationLocales?.takeIf { !it.isEmpty }?.get(0)?.toLanguageTag()
            } else {
                AppLocale.override(context)
            } ?: return null
            return entries.firstOrNull { sameLanguage(it.tag, tag) }
        }

        /** The language the app is actually showing now, whichever route got it there. */
        fun showing(context: Context): AppLanguage {
            val tag = AppLocale.of(context).toLanguageTag()
            return entries.firstOrNull { sameLanguage(it.tag, tag) } ?: ENGLISH
        }

        /** Null means "Phone language". */
        fun apply(context: Context, language: AppLanguage?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // The system recreates the activity itself when the app's locales change.
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    if (language == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(language.tag)
            } else {
                AppLocale.setOverride(context, language?.tag)
                context.findActivity()?.recreate()
            }
        }

        /**
         * `id` and `in`, `he` and `iw` are the same language under two codes, and which one
         * `Locale.getLanguage()` answers differs by Android version. Compare languages, not
         * strings, or Indonesian is "unavailable" on exactly the phones that have it.
         */
        internal fun sameLanguage(a: String, b: String): Boolean =
            Locale.forLanguageTag(a).language == Locale.forLanguageTag(b).language

        private tailrec fun Context.findActivity(): Activity? = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
    }
}
