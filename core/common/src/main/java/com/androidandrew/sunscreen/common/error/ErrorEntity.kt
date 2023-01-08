package com.androidandrew.sunscreen.common.error

sealed class ErrorEntity {

    object Network : ErrorEntity()

    object NotFound : ErrorEntity()

    object Unknown : ErrorEntity()
}