package com.androidandrew.sunscreen.ui.clothing

import com.androidandrew.sunscreen.domain.UvFactor

sealed interface ClothingEvent {
    data class Selected(val clothing: UvFactor.Clothing) : ClothingEvent
}