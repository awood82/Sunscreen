package com.androidandrew.sunscreen.util

class LocationUtil {

    companion object {
        private const val ZIP_CODE_LENGTH = 5
    }

    fun isValidZipCode(location: String): Boolean {
        return location.length == ZIP_CODE_LENGTH
                && location.toIntOrNull() != null
    }
}