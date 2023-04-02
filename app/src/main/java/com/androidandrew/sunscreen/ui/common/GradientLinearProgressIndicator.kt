package com.androidandrew.sunscreen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun GradientLinearProgressIndicator(
    progress: Float,
    progressColors: ImmutableList<Color>,
    trackColors: ImmutableList<Color>,
    modifier: Modifier = Modifier
) {
    val foregroundBrush = Brush.horizontalGradient(progressColors)
    val backgroundBrush = Brush.horizontalGradient(trackColors)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(foregroundBrush)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(1.0f - progress)
                .align(Alignment.CenterEnd)
                .background(backgroundBrush)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GradientLinearProgressIndicatorEmptyPreview() {
    MaterialTheme {
        GradientLinearProgressIndicator(
            progress = 0.0f,
            progressColors = persistentListOf(Color.White, Color.Yellow, Color.Red),
            trackColors = persistentListOf(Color.LightGray, Color.DarkGray),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GradientLinearProgressIndicatorPartialPreview() {
    MaterialTheme {
        GradientLinearProgressIndicator(
            progress = 0.6f,
            progressColors = persistentListOf(Color.White, Color.Yellow, Color.Red),
            trackColors = persistentListOf(Color.LightGray, Color.DarkGray),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GradientLinearProgressIndicatorFullPreview() {
    MaterialTheme {
        GradientLinearProgressIndicator(
            progress = 1.0f,
            progressColors = persistentListOf(Color.White, Color.Yellow, Color.Red),
            trackColors = persistentListOf(Color.LightGray, Color.DarkGray),
            modifier = Modifier.height(32.dp)
        )
    }
}