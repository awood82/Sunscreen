package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationScreen(
    onLocationValid: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = koinViewModel()
) {
    val locationBarState: LocationBarState by viewModel.locationBarState.collectAsStateWithLifecycle()
    val isLocationValid by viewModel.isLocationValid.collectAsStateWithLifecycle(initialValue = false)

    if (isLocationValid) {
        LaunchedEffect(Unit) {
            onLocationValid()
        }
    }

    LocationScreenWithState(
        locationBarState = locationBarState,
        onLocationBarEvent = { viewModel.onEvent(it) },
        modifier = modifier
    )
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
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(bottom = 64.dp)) {
            LocationBarWithState(
                uiState = locationBarState,
                onEvent = onLocationBarEvent
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LocationScreenWithStatePreview() {
    SunscreenTheme {
        LocationScreenWithState(
            locationBarState = LocationBarState(typedSoFar = "123"),
            onLocationBarEvent = {}
        )
    }
}