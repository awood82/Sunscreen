package com.androidandrew.sunscreen.ui.skintype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme

@Composable
fun FitzpatrickSkinTypeRow(
    title: String,
    description: String,
    example: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        ColoredCircleShape(color = color)
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = example,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun ColoredCircleShape(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(color)
    )
}



@Preview(showBackground = true)
@Composable
fun FitzpatrickSkinTypeRowPreview() {
    SunscreenTheme {
        FitzpatrickSkinTypeRow(
            title = "Type V",
            description = "Resistant skin. Rarely burns. Tans well.",
            example = "Dark brown skin.\nDark brown eyes.\nDark brown to black hair.",
            color = Color(0xFF332200),
            modifier = Modifier.padding(8.dp)
        )
    }
}
