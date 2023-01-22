package com.androidandrew.sunscreen.ui.clothing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.defaultBottom
import com.androidandrew.sunscreen.model.defaultTop
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClothingScreen(
    modifier: Modifier = Modifier,
    onContinuePressed: () -> Unit,
    viewModel: ClothingViewModel = koinViewModel()
) {
    val isContinuePressed by viewModel.isContinuePressed.collectAsStateWithLifecycle(initialValue = false)

    if (isContinuePressed) {
        LaunchedEffect(Unit) {
            onContinuePressed()
        }
    }

    ClothingScreen(
        onEvent = { viewModel.onEvent(it) },
        modifier = modifier
    )
}

// Define this without the ViewModel so that the Preview can render in Android Studio
@Composable
private fun ClothingScreen(
    onEvent: (ClothingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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

        Spacer(modifier = Modifier.size(16.dp))

        ClothingRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            clothingItems = listOf(
                ClothingItemData(
                    id = ClothingTop.NOTHING,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_top_nothing
                ),
                ClothingItemData(
                    id = ClothingTop.T_SHIRT,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_top_some
                ),
                ClothingItemData(
                    id = ClothingTop.LONG_SLEEVE_SHIRT,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_top_covered
                )
            ),
            onClick = { onEvent(ClothingEvent.TopSelected(it)) },
            initiallySelectedIndex = defaultTop.dbValue
        )
        ClothingRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            clothingItems = listOf(
                ClothingItemData(
                    id = ClothingBottom.NOTHING,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_bottom_nothing
                ),
                ClothingItemData(
                    id = ClothingBottom.SHORTS,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_bottom_some
                ),
                ClothingItemData(
                    id = ClothingBottom.PANTS,
                    drawableId = R.drawable.ic_launcher_foreground,
                    contentDescriptionId = R.string.clothing_bottom_covered
                )
            ),
            onClick = { onEvent(ClothingEvent.BottomSelected(it)) },
            initiallySelectedIndex = defaultBottom.dbValue
        )

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = { onEvent(ClothingEvent.ContinuePressed) }
        ) {
            Text(stringResource(id = R.string.clothing_screen_done))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ClothingScreenPreview() {
    SunscreenTheme {
        ClothingScreen(
            onEvent = {}
        )
    }
}