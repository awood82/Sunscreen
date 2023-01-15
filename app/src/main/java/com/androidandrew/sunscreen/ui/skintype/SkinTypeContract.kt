package com.androidandrew.sunscreen.ui.skintype

sealed interface SkinTypeEvent {
    data class Selected(val skinType: Int) : SkinTypeEvent
}