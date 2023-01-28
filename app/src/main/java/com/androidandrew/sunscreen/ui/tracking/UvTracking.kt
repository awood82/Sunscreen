@file:OptIn(ExperimentalMaterial3Api::class)

package com.androidandrew.sunscreen.ui.tracking

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.common.LabeledProgressTracker

@Composable
fun UvTrackingWithState(
    uiState: UvTrackingState,
    onEvent: (UvTrackingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        LabeledProgressTracker(
            progress = uiState.sunburnProgressPercent0to1,
            progressColors = listOf(
                colorResource(R.color.progress_burn_start),
                colorResource(R.color.progress_burn_center),
                colorResource(R.color.progress_burn_end)
            ),
            trackColors = listOf(
                colorResource(R.color.progress_background_start),
                colorResource(R.color.progress_background_end)
            ),
            textColor = colorResource(R.color.black),
            label = stringResource(R.string.sunburn),
            progressText = stringResource(R.string.sunburn_progress, uiState.sunburnProgressAmount)
        )

        LabeledProgressTracker(
            progress = uiState.vitaminDProgressPercent0to1,
            progressColors = listOf(
                colorResource(R.color.progress_vitamin_d_start),
                colorResource(R.color.progress_vitamin_d_end)
            ),
            trackColors = listOf(
                colorResource(R.color.progress_background_start),
                colorResource(R.color.progress_background_end)
            ),
            textColor = colorResource(R.color.black),
            label = stringResource(R.string.vitamin_d),
            progressText = stringResource(R.string.vitamin_d_progress, uiState.vitaminDProgressAmount)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.spf)) },
                singleLine = true,
                value = uiState.spfOfSunscreenAppliedToSkin,
                onValueChange = { onEvent(UvTrackingEvent.SpfChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(80.dp)
            )

            Checkbox(
                checked = uiState.isOnSnowOrWater,
                onCheckedChange = { onEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(it)) },
                modifier = Modifier.testTag("checkOnSnowOrWater")
            )

            Text(
                text = stringResource(R.string.on_snow_or_water),
            )
        }

        Button(
            enabled = uiState.isTrackingPossible,
            onClick = { onEvent(UvTrackingEvent.TrackingButtonClicked) }
        ) {
            @StringRes val buttonTextId = when (uiState.isTracking) {
                true -> R.string.stop_tracking
                false -> R.string.start_tracking
            }
            Text(text = stringResource(buttonTextId))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UvTrackingWithStatePreview() {
    MaterialTheme {
        UvTrackingWithState(
            uiState = UvTrackingState(
                isTrackingPossible = true,
                isTracking = true,
                spfOfSunscreenAppliedToSkin = "15",
                isOnSnowOrWater = true,
                sunburnProgressAmount = 30,
                sunburnProgressPercent0to1 = 0.3f,
                vitaminDProgressAmount = 1500,
                vitaminDProgressPercent0to1 = 0.8f
            ),
            onEvent = {}
        )
    }
}