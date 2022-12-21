package com.androidandrew.sunscreen.ui.burntime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.androidandrew.sunscreen.R

@Composable
fun BurnTimeWithState(
    uiState: BurnTimeState,
    modifier: Modifier = Modifier
) {
    val burnTimeString = when (uiState) {
        is BurnTimeState.Known -> { stringResource(R.string.minutes_to_burn, uiState.minutes) }
        is BurnTimeState.Unknown -> { stringResource(R.string.unknown) }
        is BurnTimeState.Unlikely -> { stringResource(R.string.unlikely) }
    }

    BurnTime(
        burnTimeString = burnTimeString,
        modifier = modifier
    )
}

@Composable
fun BurnTime(
    burnTimeString: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.could_burn),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
        Text(
            text = burnTimeString,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BurnTimeKnownMinutesPreview() {
    MaterialTheme {
        BurnTimeWithState(
            BurnTimeState.Known(35)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BurnTimeUnknownPreview() {
    MaterialTheme {
        BurnTimeWithState(
            BurnTimeState.Unknown
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BurnTimeUnlikelyPreview() {
    MaterialTheme {
        BurnTimeWithState(
            BurnTimeState.Unlikely
        )
    }
}