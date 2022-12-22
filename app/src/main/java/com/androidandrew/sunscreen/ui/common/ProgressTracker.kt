package com.androidandrew.sunscreen.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProgressTracker(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = progressColor.copy(alpha = ProgressIndicatorDefaults.IndicatorBackgroundOpacity),
    label: String = "",
    progressText: String = ""
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
    ) {
        // TODO: No gradient support built-in yet. Will wait for built-in support, or implement later.
        LinearProgressIndicator(
            progress = progress,
            color = progressColor,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = progressText,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .testTag("progress")
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProgressTrackerPreview() {
    MaterialTheme {
        ProgressTracker(
            progress = 0.6f,
            progressColor = Color.Red,
            backgroundColor = Color.Gray,
            label = "Sunburn",
            progressText = "60%"
        )
    }
}