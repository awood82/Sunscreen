package com.androidandrew.sunscreen.common.error

interface ErrorHandler {
    fun getError(throwable: Throwable): ErrorEntity
}