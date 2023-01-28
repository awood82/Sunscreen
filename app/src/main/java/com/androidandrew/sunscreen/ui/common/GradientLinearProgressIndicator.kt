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
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    val brush = Brush.horizontalGradient(progressColors)
    Box(
        modifier = modifier
            .background(trackColor)
            .fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .background(brush)
                .fillMaxWidth(progress)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GradientLinearProgressIndicatorPreview() {
    MaterialTheme {
        GradientLinearProgressIndicator(
            progress = 0.6f,
            progressColors = listOf(Color.White, Color.Yellow, Color.Red),
            trackColor = Color.Gray,
            modifier = Modifier.height(32.dp)
        )
    }
}