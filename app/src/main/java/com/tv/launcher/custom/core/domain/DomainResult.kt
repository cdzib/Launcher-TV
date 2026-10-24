package com.tv.launcher.custom.core.domain

sealed interface DomainError {
    data object AppNotFound : DomainError
    data class Unknown(val message: String?) : DomainError
}

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}
