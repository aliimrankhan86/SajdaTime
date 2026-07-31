package com.sajdatime.wear

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.sajdatime.core.AppLocale

class WearMainActivity : ComponentActivity() {

    /** Pinned to the app's own language, exactly as on the phone. See AppLocale.kt. */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    private val viewModel: WearViewModel by viewModels()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.refreshLocation() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            SajdaWearTheme {
                WearApp(viewModel)
            }
        }

        // Asked once. If the watch has no location of its own, settings synced from the
        // phone still cover it.
        locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshLocation()
    }
}
