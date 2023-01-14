package com.androidandrew.sunscreen.common.error

class ErrorHandlerImpl : ErrorHandler {
    override fun getError(throwable: Throwable): ErrorEntity {
        return ErrorEntity.Network
    }
}