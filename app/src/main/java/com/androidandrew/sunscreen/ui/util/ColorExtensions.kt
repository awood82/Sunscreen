package com.androidandrew.sunscreen.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

// https://stackoverflow.com/questions/33050999/programmatically-set-text-color-to-primary-android-textview

@SuppressLint("ResourceAsColor")
@ColorInt
fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val resolvedAttr = resolveThemeAttr(colorAttr)
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    val colorRes = if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
    return ContextCompat.getColor(this, colorRes)
//    return this.getColor(colorRes)
}

fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}