package io.github.apollointhehouse.utils

sealed class Result<out T> {
	data class Success<T>(val value: T) : Result<T>()
	data class Error(val message: String) : Result<Nothing>()

	fun getOrNull(): T? = when (this) {
		is Success -> value
		is Error -> null
	}

	fun <R> map(transform: (T) -> R): Result<R> = when (this) {
		is Success -> Success(transform(value))
		is Error -> this
	}
}
