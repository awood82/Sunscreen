package com.androidandrew.sunscreen.ui.clothing

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.model.ClothingRegion
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.ui.skintype.ColoredCircleShape
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme

data class ClothingItemData(
    val id: ClothingRegion,
    @DrawableRes val drawableId: Int,
    @StringRes val contentDescriptionId: Int,
    val backgroundColor: Color? = null
)

@Composable
fun ClothingRowRememberSelection(
    clothingItems: List<ClothingItemData>,
    onClick: (region: ClothingRegion) -> Unit,
    modifier: Modifier = Modifier,
    initiallySelectedIndex: Int
) {
    var selectedIndex by rememberSaveable { mutableStateOf(initiallySelectedIndex) }

    ClothingRow(
        clothingItems = clothingItems,
        onClick = { region ->
            val selectedItem = clothingItems.find {
                it.id == region
            }
            selectedIndex = clothingItems.indexOf(selectedItem)
            onClick(region)
        },
        selectedIndex = selectedIndex
    )
}

@Composable
fun ClothingRow(
    clothingItems: List<ClothingItemData>,
    onClick: (region: ClothingRegion) -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(
            items = clothingItems,
            key = { item -> item.id }
        ) { item ->
            ClothingItem(
                drawableId = item.drawableId,
                contentDescriptionId = item.contentDescriptionId,
                backgroundColor = item.backgroundColor ?: Color.White,
                modifier = modifier.clickable(
                    onClick = {
                        onClick(item.id)
                    }
                ),
                isSelected = clothingItems[selectedIndex].id == item.id
            )
        }
    }
}

@Composable
fun ClothingItem(
    @DrawableRes drawableId: Int,
    @StringRes contentDescriptionId: Int,
    backgroundColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = if (isSelected) {
            modifier
                .border(4.dp, Color.Red)
                .padding(8.dp)
        } else {
            modifier.padding(8.dp)
        }
    ) {
        ColoredCircleShape(color = backgroundColor)
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = stringResource(id = contentDescriptionId)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ClothingRowPreview() {
    SunscreenTheme {
        ClothingRow(
            clothingItems = listOf(
                ClothingItemData(ClothingTop.T_SHIRT, R.drawable.ic_launcher_foreground, R.string.clothing_top_some, Color.Red),
                ClothingItemData(ClothingTop.T_SHIRT, R.drawable.ic_launcher_foreground, R.string.clothing_top_some),
                ClothingItemData(ClothingTop.T_SHIRT, R.drawable.ic_launcher_foreground, R.string.clothing_top_some, Color.Cyan)
            ),
            onClick = {},
            selectedIndex = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClothingItemSelectedPreview() {
    SunscreenTheme {
        ClothingItem(
            drawableId = R.drawable.ic_launcher_foreground,
            contentDescriptionId = R.string.clothing_top_some,
            backgroundColor = Color.Red,
            isSelected = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClothingItemUnselectedPreview() {
    SunscreenTheme {
        ClothingItem(
            drawableId = R.drawable.ic_launcher_foreground,
            contentDescriptionId = R.string.clothing_top_some,
            backgroundColor = Color.Red,
            isSelected = false
        )
    }
}