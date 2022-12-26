package com.androidandrew.sunscreen.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import com.androidandrew.sunscreen.R

enum class AppDestination(
    @StringRes val titleResId: Int,
    val args: List<NamedNavArgument>
) {
    Location(titleResId = R.string.app_name, args = emptyList()),
    Main(titleResId = R.string.app_name, args = emptyList())
}