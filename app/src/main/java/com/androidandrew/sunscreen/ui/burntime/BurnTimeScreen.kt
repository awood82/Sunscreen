package com.androidandrew.sunscreen.ui.burntime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun BurnTimeScreen(
    viewModel: MainViewModel = get(),   //TODO: BurnTimeViewModel
    modifier: Modifier = Modifier
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val burnTimeUiState: BurnTimeUiState by viewModel.burnTimeUiState.collectAsStateWithLifecycle()

    BurnTimeWithState(uiState = burnTimeUiState)
}

@Composable
fun BurnTimeWithState(
    uiState: BurnTimeUiState,
    modifier: Modifier = Modifier
) {
    val burnTimeString = when (uiState) {
        is BurnTimeUiState.Known -> { stringResource(R.string.minutes_to_burn, uiState.minutes) }
        is BurnTimeUiState.Unknown -> { stringResource(R.string.unknown) }
        is BurnTimeUiState.Unlikely -> { stringResource(R.string.unlikely) }
    }

    BurnTime(
        burnTimeString = burnTimeString,
        modifier = modifier)
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
fun BurnTimePreview() {
    BurnTime(
        burnTimeString = "35 minutes"
    )
}