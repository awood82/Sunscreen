package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LocationScreen(
    onLocationValid: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = koinViewModel()
) {
    val locationBarState: LocationBarState by viewModel.locationBarState.collectAsStateWithLifecycle()
    val isLocationValid by viewModel.isLocationValid.collectAsStateWithLifecycle()

    when (isLocationValid) {
        true -> {
            LaunchedEffect(true) {
                onLocationValid()
            }
        }
        false -> {
            LocationScreenWithState(
                locationBarState = locationBarState,
                onLocationBarEvent = { viewModel.onEvent(it) },
                modifier = modifier
            )
        }
    }
}

@Composable
fun LocationScreenWithState(
    locationBarState: LocationBarState,
    onLocationBarEvent: (LocationBarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box {
            Image(
                painter = painterResource(R.drawable.starryai_0_309817728_4_0_photo),
                contentDescription = stringResource(R.string.location_background_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
        Box(modifier = modifier.padding(bottom = 64.dp)) {
            LocationBarWithState(
                uiState = locationBarState,
                onEvent = onLocationBarEvent,
                modifier = modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

