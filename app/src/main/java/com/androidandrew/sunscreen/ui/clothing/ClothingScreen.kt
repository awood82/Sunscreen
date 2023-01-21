package com.androidandrew.sunscreen.ui.clothing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.domain.UvFactor
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClothingScreen(
    modifier: Modifier = Modifier,
    onClothingSelected: () -> Unit,
    viewModel: ClothingViewModel = koinViewModel()
) {
    val isClothingSelected by viewModel.isClothingSelected.collectAsStateWithLifecycle(initialValue = false)

    if (isClothingSelected) {
        LaunchedEffect(Unit) {
            onClothingSelected()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.clothing_screen_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = stringResource(R.string.clothing_screen_instructions),
            style = MaterialTheme.typography.headlineSmall
        )
        // TODO: Add Skip button
        ClothingRow(
            description = stringResource(R.string.clothing_shorts_no_shirt),
            modifier = Modifier.clickable {
                viewModel.onEvent(ClothingEvent.Selected(UvFactor.Clothing.SHORTS_NO_SHIRT))
            }
        )
        ClothingRow(
            description = stringResource(R.string.clothing_pants_no_shirt),
            modifier = Modifier.clickable {
                viewModel.onEvent(ClothingEvent.Selected(UvFactor.Clothing.PANTS_NO_SHIRT))
            }
        )
        ClothingRow(
            description = stringResource(R.string.clothing_shorts_t_shirt),
            modifier = Modifier.clickable {
                viewModel.onEvent(ClothingEvent.Selected(UvFactor.Clothing.SHORTS_T_SHIRT))
            }
        )
        ClothingRow(
            description = stringResource(R.string.clothing_pants_t_shirt),
            modifier = Modifier.clickable {
                viewModel.onEvent(ClothingEvent.Selected(UvFactor.Clothing.PANTS_T_SHIRT))
            }
        )
        ClothingRow(
            description = stringResource(R.string.clothing_pants_long_sleeve_shirt),
            modifier = Modifier.clickable {
                viewModel.onEvent(ClothingEvent.Selected(UvFactor.Clothing.PANTS_LONG_SLEEVE_SHIRT))
            }
        )
    }
}

@Composable
fun ClothingRow(
    description: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = description,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ClothingScreenPreview() {
    SunscreenTheme {
        ClothingScreen(
            onClothingSelected = {}
        )
    }
}