@file:OptIn(ExperimentalMaterial3Api::class)

package com.androidandrew.sunscreen.ui.tracking

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.common.LabeledProgressTracker
import com.androidandrew.sunscreen.ui.theme.*
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
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
            progressColors = persistentListOf(
                ProgressBurnStart,
                ProgressBurnCenter,
                ProgressBurnEnd
            ),
            trackColors = persistentListOf(
                ProgressBackgroundStart,
                ProgressBackgroundEnd
            ),
            textColor = OnProgressBar,
            label = stringResource(R.string.sunburn),
            progressText = stringResource(R.string.sunburn_progress, uiState.sunburnProgressAmount)
        )

        LabeledProgressTracker(
            progress = uiState.vitaminDProgressPercent0to1,
            progressColors = persistentListOf(
                ProgressVitaminDStart,
                ProgressVitaminDEnd
            ),
            trackColors = persistentListOf(
                ProgressBackgroundStart,
                ProgressBackgroundEnd
            ),
            textColor = OnProgressBar,
            label = stringResource(R.string.vitamin_d),
            progressText = stringResource(R.string.vitamin_d_progress, uiState.vitaminDProgressAmount)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.skin_icon),
                contentDescription = stringResource(R.string.skin_type_title),
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clickable { onEvent(UvTrackingEvent.SkinTypeClicked) }
            )

            Image(
                painterResource(R.drawable.shirt_icon),
                contentDescription = stringResource(R.string.clothing_screen_title),
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clickable { onEvent(UvTrackingEvent.ClothingClicked) }
            )

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