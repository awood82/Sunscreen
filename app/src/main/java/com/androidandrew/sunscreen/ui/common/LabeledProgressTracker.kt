package com.androidandrew.sunscreen.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun LabeledProgressTracker(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColors: ImmutableList<Color> = persistentListOf(ProgressIndicatorDefaults.linearColor, ProgressIndicatorDefaults.linearColor),
    trackColors: ImmutableList<Color> = persistentListOf(ProgressIndicatorDefaults.linearTrackColor, ProgressIndicatorDefaults.linearTrackColor),
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    label: String = "",
    progressText: String = ""
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        GradientLinearProgressIndicator(
            progress = progress,
            progressColors = progressColors,
            trackColors = trackColors,
            modifier = Modifier
                .fillMaxHeight()
        )
        Text(
            text = label,
            color = textColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )
        Text(
            text = progressText,
            color = textColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .testTag("progress")
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProgressTrackerPreview() {
    MaterialTheme {
        LabeledProgressTracker(
            progress = 0.6f,
            progressColors = persistentListOf(Color.White, Color.Yellow, Color.Red),
            trackColors = persistentListOf(Color.LightGray, Color.DarkGray),
            textColor = Color.Black,
            label = "Sunburn",
            progressText = "60%"
        )
    }
}