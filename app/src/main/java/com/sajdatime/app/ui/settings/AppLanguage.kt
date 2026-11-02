package com.sajdatime.app.ui.settings

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.sajdatime.app.R
import com.sajdatime.core.AppLocale
import java.util.Locale

/**
 * The languages the in-app picker offers.
 *
 * **The default is not a language, it is "follow the phone".** Most people never open this
 * picker, and for them the app already speaks the phone's language when it has words in it
 * ([AppLocale] reads the language back from the resources). So [chosen] is null until the
 * user picks something, and the picker shows "Phone language" ticked, not "English". Before
 * 1.5.0 it showed English ticked on a Turkish phone running the Turkish app.
 *
 * **Two routes, one outcome.** From Android 13 the system keeps a per-app language
 * (`LocaleManager`, declared to it by `res/xml/locales_config.xml`). Below 13 there is nothing
 * to hand us, and many phones in this app's audience are below 13 and set to English while
 * their owner reads Urdu, so the choice is stored by [AppLocale.setOverride] and applied when
 * each context is wrapped. Either way the app's language is still the language of the
 * resources it resolves to, never the stored tag itself. (Built on the 8 Oct 2026 branch
 * `claude/customer-review-pain-points-9d35kl`, brought into 1.5.0 on 19 Oct 2026; before that
 * the Language row did not exist at all below Android 13.)
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
enum class AppLanguage(val tag: String, val nativeName: String, val englishName: String) {
    // Every language is named twice, on purpose: [nativeName] in its own language and script, so a
    // user who cannot read the current language can find theirs, and [englishName] in English in
    // every app language, so someone who cannot read that script can still tell which it is.
    // Constants, not resources, because they must never translate (owner, 25 Oct 2026: "write their
    // name also urdu in their own text then write urdu in English also").
    ENGLISH("en", "English", "English"),
    URDU("ur", "اردو", "Urdu"),
    TURKISH("tr", "Türkçe", "Turkish"),
    INDONESIAN("id", "Bahasa Indonesia", "Indonesian"),
    // Added 17 Oct 2026 after a 1 star review asked for it ("إضافة اللغة العربية"). Claude's
    // translation, checked by Claude, as the owner decided for every language on 9 Oct 2026.
    // Choosing it turns the app right to left through AppLocale, with Western digits: its tag is
    // ar-u-nu-latn (see values-ar/strings.xml for why).
    ARABIC("ar", "العربية", "Arabic"),
    // Added 30 Oct 2026 at the owner's request. Claude's translation, checked by Claude. Its tag is
    // bn-u-nu-latn, Western digits, for the same reason as Arabic (values-bn/strings.xml says why).
    // Adding a language: docs/design-system/06-content-and-language.md, "Adding a language".
    BENGALI("bn", "বাংলা", "Bengali"),
    ;

    /** "اردو (Urdu)", or just "English" where the two names are the same. Not isolated: callers do that. */
    fun namePair(context: Context): String =
        if (nativeName == englishName) nativeName else context.getString(R.string.language_name_pair, nativeName, englishName)

    /** True when this language's words are installed on this phone. */
    fun isAvailable(context: Context): Boolean {
        if (this == ENGLISH) return true
        val config = Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
        val resolved = context.createConfigurationContext(config).getString(com.sajdatime.core.R.string.app_language_tag)
        return normalise(Locale.forLanguageTag(resolved).language) == normalise(Locale.forLanguageTag(tag).language)
    }

    companion object {
        /** What the user picked, or null when they have not and the app follows the phone. */
        fun chosen(context: Context): AppLanguage? {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)
                    ?.applicationLocales?.takeIf { !it.isEmpty }?.get(0)
            } else {
                AppLocale.override(context)?.let(Locale::forLanguageTag)
            } ?: return null
            return fromLocale(locale)
        }

        /**
         * The language following the phone would give: what the app shows when nothing is chosen,
         * otherwise the phone's whole language list resolved against our resources, the way Android
         * picks the app's words (a phone set to French then Urdu gets Urdu, not English). The list
         * comes from LocaleManagerCompat because on Android 13+ the app's own choice reaches
         * Resources.getSystem() (HANDOVER §15 lesson 181).
         */
        fun phone(context: Context): AppLanguage {
            if (chosen(context) == null) return showing(context)
            val tags = androidx.core.app.LocaleManagerCompat.getSystemLocales(context).toLanguageTags()
            val config = Configuration(context.resources.configuration).apply { setLocales(LocaleList.forLanguageTags(tags)) }
            val resolved = context.createConfigurationContext(config).getString(com.sajdatime.core.R.string.app_language_tag)
            return fromLocale(Locale.forLanguageTag(resolved))
        }

        /** The language the app is actually showing now, whichever route got it there. */
        fun showing(context: Context): AppLanguage = fromLocale(AppLocale.of(context))

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

        /** Null means "Phone language". */
        fun apply(context: Context, language: AppLanguage?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // The system recreates the activity itself when the app's locales change.
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    if (language == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(language.tag)
            } else {
                AppLocale.setOverride(context, language?.tag)
                // MainActivity sets the window's direction again in onCreate: recreate() alone kept the
                // old direction on Android 12 (HANDOVER §15 lesson 152).
                context.findActivity()?.recreate()
            }
        }

        private tailrec fun Context.findActivity(): Activity? = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
    }
}
