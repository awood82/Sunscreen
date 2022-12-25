package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.androidandrew.sunscreen.R

@Composable
fun LocationBarWithState(
    uiState: LocationBarState,
    onEvent: (LocationBarEvent) -> Unit
) {
    LocationBar(
        value = uiState.typedSoFar,
        onValueChange = { onEvent(LocationBarEvent.TextChanged(it)) },
        onLocationSearched = { onEvent(LocationBarEvent.LocationSearched(it)) }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocationBar(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSearched: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(stringResource(R.string.current_location)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                modifier = modifier
                    .testTag("locationBarSearch")
                    .clickable {
                        onLocationSearched(value)
                    }
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
                onSearch = {
                    onLocationSearched(value)
                    keyboardController?.hide()
                }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("locationText"),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background
        )
    )
}

@Preview(showBackground = true)
@Composable
fun LocationBarPreview() {
    LocationBar(
        value = "",
        onValueChange = {},
        onLocationSearched = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LocationBarPreviewWithText() {
    LocationBar(
        value = "12345",
        onValueChange = {},
        onLocationSearched = {}
    )
}