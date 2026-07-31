package com.sajdatime.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * The language the app's own words are actually in, which is not the same thing as the
 * language the device is set to.
 *
 * Android answers those two questions from different places. Text comes from whichever
 * `values-*` folder matches, falling back to the default one. Numbers, clock times and
 * dates come from `Locale.getDefault()`, which is the device setting and takes no notice
 * of whether a translation exists. While the app ships in one language those two answers
 * disagree for every user who has set their phone to anything else, and running it that
 * way is how the following were found:
 *
 *  - On an Arabic phone the Hijri date "17 Safar 1448" rendered as "١٤٤٨ Safar ١٧".
 *    Arabic-Indic digits are strongly right-to-left, the English month name between them
 *    is left-to-right, and the bidi algorithm duly reordered a date nobody had marked up.
 *    It reads as a different date rather than as a rendering fault, which is the
 *    dangerous kind of wrong.
 *  - "118° from true north. The Kaaba is about 4822 km away." became
 *    "km ٤٨٢٢ from true north. The Kaaba is about ١١٨°" with the full stop stranded on
 *    the next line. Any sentence that opens or closes with a number scrambles the same
 *    way, which on this screen is most of them.
 *  - The countdown on the home card overflowed onto two lines in both Arabic and Bengali.
 *    Those digits are wider than Latin ones and the card was sized for Latin.
 *  - Two numbering systems appeared on one screen. `String.format` and
 *    `Resources.getString` go through ICU, which gives Bengali a Bengali zero, while the
 *    desugared `java.time` carries its own CLDR data in which Bengali's zero is Latin. So
 *    the countdown read "০২:১৮:১৯" directly above prayer times reading "2:50 AM".
 *
 * The fix is to format in the language the words are in. [of] reads that language back
 * out of the resources themselves rather than hardcoding English, so the day a
 * translation ships it formats to that translation's conventions with no code change,
 * and the two cannot drift apart. `LocaleDisciplineTest` fails the build if a translation
 * is added without declaring its tag, or if anything reaches for `Locale.getDefault()`.
 *
 * Deliberately *not* `Locale.getDefault()` anywhere in the app except where the platform
 * demands it. If you are reaching for it, you almost certainly want this instead.
 *
 * To see what a right-to-left translation will look like before one exists, run
 * `./gradlew installRtl`. That build type overrides `app_language_tag` with the `ar-XB`
 * pseudolocale, which is enough to flip the entire app, because everything below follows
 * this one string.
 */
object AppLocale {

    /** The locale of the strings the app is currently showing. */
    fun of(context: Context): Locale =
        Locale.forLanguageTag(context.getString(R.string.app_language_tag))

    /**
     * A context whose whole configuration is pinned to [of] — resources, the locale that
     * `Resources.getString(id, args)` formats `%d` with, and the layout direction.
     *
     * [of] alone is not enough, and finding that out took running it twice. Passing the
     * right locale to `DateTimeFormatter` and `String.format` fixed the clock and the
     * countdown, and left the Qibla screen exactly as broken as before: everything there
     * goes through `stringResource`, which lands in `Resources.getString(id, args)`, which
     * formats from the *configuration* locale and takes no notice of which `values-`
     * folder the text it just loaded actually came from. There is no argument to change
     * that. The configuration is the only lever.
     *
     * Pinning it also settles layout direction, which is the other half of the same
     * problem. An English sentence laid out right-to-left has its leading number and its
     * closing full stop moved to the far end by the bidi algorithm, which is why
     * "118° from true north. The Kaaba is about 4822 km away." came out as
     * "km 4822 from true north. The Kaaba is about 118°" with the full stop alone on the
     * next line. Bidi is behaving correctly there; it is being told the paragraph is
     * Arabic when every word in it is English.
     *
     * Note what this does *not* do: it does not override the user. The tag is read from
     * the resources, so the day `values-ar/strings.xml` ships, an Arabic phone resolves
     * the tag to "ar", this pins the configuration to Arabic, and the app goes
     * right-to-left with Arabic-Indic digits — which is then correct, and arrives with no
     * code change. The pin only ever holds the app to a language it is genuinely written
     * in.
     */
    fun wrap(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        // setLocale also sets the layout direction from the locale, which is the point.
        config.setLocale(of(context))
        return context.createConfigurationContext(config)
    }
}
