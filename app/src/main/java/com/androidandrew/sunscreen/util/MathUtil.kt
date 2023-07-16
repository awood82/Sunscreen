package com.androidandrew.sunscreen.util

/**
 * Converts a percent 0-1f to 0-100 (or higher)
 */
fun Float.percentToInt(): Int {
    return (this * 100.0f).toInt()
}