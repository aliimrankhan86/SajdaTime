package com.sajdatime.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

class WearMainActivity : ComponentActivity() {

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
