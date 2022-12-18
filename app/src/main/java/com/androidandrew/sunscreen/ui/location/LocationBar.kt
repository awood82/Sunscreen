package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.androidandrew.sunscreen.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocationBar(
    onLocationSearched: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = ""
) {
    var locationEntry by rememberSaveable { mutableStateOf(initialText) }
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = locationEntry,
        onValueChange = { locationEntry = it },
        singleLine = true,
        label = { Text(stringResource(R.string.current_location)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                modifier = modifier.clickable {
                    onLocationSearched(locationEntry)
                }
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
                onSearch = {
                    onLocationSearched(locationEntry)
                    keyboardController?.hide()
                }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun LocationBarPreview() {
    LocationBar(
        onLocationSearched = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LocationBarPreviewWithText() {
    LocationBar(
        onLocationSearched = {},
        initialText = "12345"
    )
}