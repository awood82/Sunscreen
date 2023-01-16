package com.androidandrew.sunscreen.ui.skintype

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SkinTypeScreen(
    modifier: Modifier = Modifier,
    onSkinTypeSelected: () -> Unit,
    viewModel: SkinTypeViewModel = koinViewModel()
) {
    val isSkinTypeSelected by viewModel.isSkinTypeSelected.collectAsStateWithLifecycle()

    if (isSkinTypeSelected) {
        LaunchedEffect(true) {
            onSkinTypeSelected()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.skin_type_instructions),
            style = MaterialTheme.typography.headlineSmall
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_1_title),
            description = stringResource(R.string.type_1_description),
            example = stringResource(R.string.type_1_example),
            color = SkinType1,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(1))
            }
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_2_title),
            description = stringResource(R.string.type_2_description),
            example = stringResource(R.string.type_2_example),
            color = SkinType2,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(2))
            }
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_3_title),
            description = stringResource(R.string.type_3_description),
            example = stringResource(R.string.type_3_example),
            color = SkinType3,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(3))
            }
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_4_title),
            description = stringResource(R.string.type_4_description),
            example = stringResource(R.string.type_4_example),
            color = SkinType4,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(4))
            }
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_5_title),
            description = stringResource(R.string.type_5_description),
            example = stringResource(R.string.type_5_example),
            color = SkinType5,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(5))
            }
        )
        FitzpatrickSkinTypeRow(
            title = stringResource(R.string.type_6_title),
            description = stringResource(R.string.type_6_description),
            example = stringResource(R.string.type_6_example),
            color = SkinType6,
            modifier = Modifier.clickable {
                viewModel.onEvent(SkinTypeEvent.Selected(6))
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SkinTypeScreenPreview() {
    SunscreenTheme {
        SkinTypeScreen(
            onSkinTypeSelected = {}
        )
    }
}