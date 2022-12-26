package com.androidandrew.sunscreen.ui.main

sealed interface AppState {
    object Loading: AppState
    object NotOnboarded: AppState
    object Onboarded: AppState
}