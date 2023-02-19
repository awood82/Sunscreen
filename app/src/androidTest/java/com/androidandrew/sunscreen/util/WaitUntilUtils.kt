// Copyright 2022 Google LLC.
// SPDX-License-Identifier: Apache-2.0

package com.androidandrew.sunscreen.util

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import kotlinx.coroutines.runBlocking

private const val MAX_WAIT_DELAY = 5_000L

fun ComposeContentTestRule.waitUntilNodeCount(
    matcher: SemanticsMatcher,
    count: Int,
    timeoutMillis: Long = MAX_WAIT_DELAY
) {
    runBlocking {
        awaitIdle()
    }
    this.waitUntil(timeoutMillis) {
        this.onAllNodes(matcher).fetchSemanticsNodes().size == count
    }
    runBlocking {
        awaitIdle()
    }
}

fun ComposeContentTestRule.waitUntilExists(
    matcher: SemanticsMatcher,
    timeoutMillis: Long = MAX_WAIT_DELAY
) {
    return this.waitUntilNodeCount(matcher, 1, timeoutMillis)
}

fun ComposeContentTestRule.waitUntilDoesNotExist(
    matcher: SemanticsMatcher,
    timeoutMillis: Long = MAX_WAIT_DELAY
) {
    return this.waitUntilNodeCount(matcher, 0, timeoutMillis)
}