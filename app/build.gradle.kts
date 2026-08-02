import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// AGP 9 has built-in Kotlin support, so the kotlin-android plugin is no longer applied.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Upload key for Google Play. keystore.properties holds the password and is gitignored,
// so a fresh clone has no key and simply builds unsigned — which is what CI and anyone
// but the owner wants. See docs/RELEASING.md.
val keystoreProperties = rootProject.file("keystore.properties").takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use(::load) } }

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.sajdatime.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sajdatime.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        keystoreProperties?.let { props ->
            create("release") {
                storeFile = file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        // `./gradlew installRtl` — a debug build that runs entirely right-to-left, for
        // previewing what a translation into Arabic or Urdu will actually look like before
        // one exists. See core/AppLocale.kt; docs/HANDOVER.md §10 has what it has caught.
        create("rtl") {
            initWith(getByName("debug"))
            // :core has no rtl build type of its own and does not need one.
            matchingFallbacks += "debug"
            // src/rtl/res/values/strings.xml overrides core's app_language_tag, and also
            // app_name so this is tellable apart on the launcher. Read that file first.
            //
            // The suffix is here for the same reason `sideload` has one, and it was added
            // the first time anyone tried to run this on a real phone: a debug-signed
            // `com.sajdatime.app` cannot install alongside the Play build, so without a
            // suffix the only way to preview RTL on a phone that has SajdaTime from Play
            // is to uninstall it — which `docs/RELEASING.md` records as the thing that
            // resets a closed tester and costs the fortnight. Every tester's phone, and
            // the owner's two, are exactly that phone. Before this, `./gradlew installRtl`
            // was runnable only on an emulator, which is why RTL had never once been seen
            // on real hardware.
            applicationIdSuffix = ".rtl"
            versionNameSuffix = "-rtl"
        }

        // `./gradlew installSideload` — for putting a test build on a phone that already
        // has the Play version installed, which is the owner's phone and every tester's.
        //
        // It exists because the two cannot coexist without it. Same applicationId and a
        // different signature means Android refuses the install, and the only way through
        // is uninstalling the Play copy — which `docs/RELEASING.md` records as the thing
        // that actually costs the fortnight: Google's own rejection wording is "testers
        // opted in, tested for less than 14 days, and then opted out", and an early
        // uninstall resets that tester rather than merely failing to count.
        //
        // The suffix is the whole mechanism, so this lands as a separate app with its own
        // icon, its own settings and its own notifications, touching nothing.
        //
        // Known limitation, and the reason `debug` was left alone: a suffixed package
        // cannot talk to the watch. The Wear Data Layer matches on package name, so
        // phone-to-watch sync is dead in this build type. Use plain `installDebug` on the
        // emulators for anything involving the watch — that pairing is exactly where this
        // project's worst bug once lived, so do not test it here.
        create("sideload") {
            initWith(getByName("debug"))
            matchingFallbacks += "debug"
            applicationIdSuffix = ".sideload"
            versionNameSuffix = "-sideload"
        }
    }

    compileOptions {
        // Desugaring gives us java.time (incl. HijrahDate) all the way down to API 24.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(project(":core"))
    // Publishes settings to a paired Wear OS watch. Local device-to-device only.
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.fragment)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    // The android.jar used by unit tests stubs org.json and every call throws. This puts
    // a real implementation on the test classpath so JSON parsing can be tested locally.
    testImplementation(libs.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
