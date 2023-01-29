package com.androidandrew.sunscreen.util

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
        .onNodeWithContentDescriptionId(@StringRes id: Int, format: Int) : SemanticsNodeInteraction {
    return onNodeWithContentDescription(activity.getString(id, format))
}

fun ComposeContentTestRule.onNodeWithStringId(@StringRes id: Int) : SemanticsNodeInteraction {
    val activity = (this as AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>).activity
    return onNodeWithText(activity.getString(id))
}

fun ComposeContentTestRule.onNodeWithContentDescriptionId(@StringRes id: Int) : SemanticsNodeInteraction {
    val activity = (this as AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>).activity
    return onNodeWithContentDescription(activity.getString(id))
//    return (this as AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>).activity.onNodeWithContentDescriptionId(id)
}

fun ComposeContentTestRule.setOrientation(orientation: Int) {
    val activity = (this as AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>).activity
    activity.requestedOrientation = orientation
}