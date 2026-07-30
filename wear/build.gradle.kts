plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Wear OS companion.
 *
 * The watch app is standalone: it calculates prayer times and the Qibla itself using the
 * shared :core module, so it keeps working when the phone is out of range or switched
 * off. Settings arrive from the phone over the Data Layer when the two are connected,
 * and fall back to sensible defaults when they are not.
 */
android {
    namespace = "com.sajdatime.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sajdatime.app"
        // Wear OS 3 and later. Older watches run a different app model entirely and are
        // not worth the maintenance for a solo project.
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
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
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Wear-specific Compose. The watch uses its own Material library, not the phone's.
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)

    // Tiles: the swipe-to-see surface next to the watch face.
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.protolayout)
    implementation(libs.androidx.protolayout.expression)
    implementation(libs.androidx.wear.tiles.material)
    // Bridges Kotlin coroutines to the ListenableFuture that TileService requires.
    implementation(libs.kotlinx.coroutines.guava)

    // Data Layer, the only way to move settings between phone and watch.
    implementation(libs.play.services.wearable)
    // play-services-wearable still pulls in a 2019-era androidx.fragment, which trips
    // the Activity Result API. Pinned forward explicitly.
    implementation(libs.androidx.fragment)

    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}
