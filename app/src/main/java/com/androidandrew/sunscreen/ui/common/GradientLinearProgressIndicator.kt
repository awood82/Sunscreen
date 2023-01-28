package com.androidandrew.sunscreen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GradientLinearProgressIndicator(
    progress: Float,
    progressColors: List<Color>,
    trackColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val foregroundBrush = Brush.horizontalGradient(progressColors)
    val backgroundBrush = Brush.horizontalGradient(trackColors)
    Box(
        modifier = modifier
            .background(backgroundBrush)
            .fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .background(foregroundBrush)
                .fillMaxWidth(progress)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GradientLinearProgressIndicatorEmptyPreview() {
    MaterialTheme {
        GradientLinearProgressIndicator(
            progress = 0.0f,
            progressColors = listOf(Color.White, Color.Yellow, Color.Red),
            trackColors = listOf(Color.LightGray, Color.DarkGray),
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
            progressColors = listOf(Color.White, Color.Yellow, Color.Red),
            trackColors = listOf(Color.DarkGray, Color.LightGray),
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
            progressColors = listOf(Color.White, Color.Yellow, Color.Red),
            trackColors = listOf(Color.DarkGray, Color.LightGray),
            modifier = Modifier.height(32.dp)
        )
    }
}