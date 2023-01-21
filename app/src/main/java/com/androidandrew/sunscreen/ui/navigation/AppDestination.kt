package com.androidandrew.sunscreen.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import com.androidandrew.sunscreen.R

enum class AppDestination(
    @StringRes val titleResId: Int,
    val args: List<NamedNavArgument>
) {
    Location(titleResId = R.string.location_title, args = emptyList()),
    SkinType(titleResId = R.string.skin_type_title, args = emptyList()),
    Clothing(titleResId = R.string.clothing_screen_title, args = emptyList()),
    Main(titleResId = R.string.app_name, args = emptyList())
}