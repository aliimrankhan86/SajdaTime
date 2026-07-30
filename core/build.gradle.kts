plugins {
    alias(libs.plugins.android.library)
}

/**
 * Shared calculation module. Phone and watch both depend on it.
 *
 * Nothing in here imports anything from the android.* namespace. It is an Android library
 * only so that AGP's built-in Kotlin support and core library desugaring apply; the code
 * itself is plain Kotlin and lifts straight into a Kotlin Multiplatform or iOS target.
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

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    api(libs.adhan)

    testImplementation(libs.junit)
}
