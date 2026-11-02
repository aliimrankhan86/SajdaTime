import org.gradle.api.tasks.PathSensitivity

plugins {
    alias(libs.plugins.android.library)
}

/**
 * Shared calculation module. Phone and watch both depend on it.
 *
 * Almost nothing in here imports from the android.* namespace: only AppLocale.kt and
 * PrayerLabels.kt take a Context (checked 23 Oct 2026). It is an Android library so that AGP's
 * built-in Kotlin support and core library desugaring apply. For a future iPhone version
 * (CLAUDE.md, the iPhone hard rule) those two files, java.time and the Java adhan library
 * would need replacing; keep new code here free of android.* so the list does not grow.
 */
android {
    namespace = "com.sajdatime.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// LocaleDisciplineTest polices the *whole repository* from here: it walks every module's
// main sources looking for Locale.getDefault(), and every module's res folders looking for a
// translation that forgot to declare app_language_tag. Gradle cannot infer any of that —
// :core:test depends on :core's own sources and nothing else — so the task was UP-TO-DATE and
// skipped in exactly the situation the guard exists for.
//
// Not a theory. Measured on 2 Aug 2026: adding `Locale.getDefault()` to a file in
// app/src/main/ and running `./gradlew :core:testDebugUnitTest` printed UP-TO-DATE and BUILD
// SUCCESSFUL. The same command with --rerun-tasks failed on the very same working tree. A
// guard that reports success while not running is worse than no guard, and this is the second
// time this exact trap has been found in this project — app/build.gradle.kts carries the same
// declaration for NoTranslationsYetTest, and fixing it there in June did not fix it here.
//
// The cost is deliberate: any edit to any module's main sources re-runs :core's tests. They
// take about eight seconds, and a guard that only runs when it is convenient is not a guard.
tasks.withType<Test>().configureEach {
    inputs.files(
        rootProject.fileTree(rootProject.projectDir) {
            include("*/src/main/**/*.kt", "*/src/**/res/values*/strings.xml")
        },
    ).withPropertyName("localePolicySources").withPathSensitivity(PathSensitivity.RELATIVE)
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    api(libs.adhan)

    testImplementation(libs.junit)
}
