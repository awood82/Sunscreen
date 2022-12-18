package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LocationScreen(
    locationViewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box {
            LocationBar(
                onLocationSearched = { locationViewModel.onSearchLocation(it) },
                modifier = modifier.align(Alignment.Center)
            )
        }
    }
}

