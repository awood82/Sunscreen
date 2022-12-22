package com.androidandrew.sunscreen.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
        // Tracking Button
        Button(
            enabled = uiState.buttonEnabled,
            onClick = { onEvent(UvTrackingEvent.TrackingButtonClicked) }
        ) {
            Text(text = stringResource(uiState.buttonLabel))
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SPF TextField
            OutlinedTextField(
                label = { Text(stringResource(R.string.spf)) },
                singleLine = true,
                value = uiState.spf,
                onValueChange = { onEvent(UvTrackingEvent.SpfChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(80.dp)
            )
            // On snow or water CheckBox
            Checkbox(
                checked = uiState.isOnSnowOrWater,
                onCheckedChange = { onEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(it)) },
                modifier = Modifier.testTag("checkOnSnowOrWater")
            )
            Text(
                text = stringResource(R.string.on_snow_or_water),
            )
        }

        // Tracking Sunburn
        LabeledProgressTracker(
            progress = uiState.sunburnProgress0to1,
            progressColor = colorResource(R.color.progress_burn_end),
            backgroundColor = colorResource(R.color.progress_background_end),
            label = stringResource(R.string.sunburn),
            progressText = stringResource(R.string.sunburn_progress, uiState.sunburnProgressLabelMinusUnits)
        )

        // Tracking Vitamin D
        LabeledProgressTracker(
            progress = uiState.vitaminDProgress0to1,
            progressColor = colorResource(R.color.progress_vitamin_d_end),
            backgroundColor = colorResource(R.color.progress_background_end),
            label = stringResource(R.string.vitamin_d),
            progressText = stringResource(R.string.vitamin_d_progress, uiState.vitaminDProgressLabelMinusUnits)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun UvTrackingWithStatePreview() {
    MaterialTheme {
        UvTrackingWithState(
            uiState = UvTrackingState(
                buttonLabel = R.string.stop_tracking,
                buttonEnabled = true,
                spf = "15",
                isOnSnowOrWater = true,
                sunburnProgressLabelMinusUnits = 30,
                sunburnProgress0to1 = 0.3f,
                vitaminDProgressLabelMinusUnits = 1500,
                vitaminDProgress0to1 = 0.8f
            ),
            onEvent = {}
        )
    }
}