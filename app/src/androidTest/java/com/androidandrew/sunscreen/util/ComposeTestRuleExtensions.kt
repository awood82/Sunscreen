package com.androidandrew.sunscreen.util

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
        .onNodeWithStringId(@StringRes id: Int) : SemanticsNodeInteraction {
    return onNodeWithText(activity.getString(id))
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
        .onNodeWithStringId(@StringRes id: Int, format: String) : SemanticsNodeInteraction {
    return onNodeWithText(activity.getString(id, format))
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
        .onNodeWithStringId(@StringRes id: Int, format: Int) : SemanticsNodeInteraction {
    return onNodeWithText(activity.getString(id, format))
}