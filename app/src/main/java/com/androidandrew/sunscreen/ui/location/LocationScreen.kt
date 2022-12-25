package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LocationScreen(
    locationViewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    var locationEntry by rememberSaveable { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize()) {
        Box {
            LocationBar(
                value = locationEntry,
                onValueChange = { locationEntry = it },
                onLocationSearched = { locationViewModel.onSearchLocation(it) },
                modifier = modifier.align(Alignment.Center)
            )
        }
    }
}

