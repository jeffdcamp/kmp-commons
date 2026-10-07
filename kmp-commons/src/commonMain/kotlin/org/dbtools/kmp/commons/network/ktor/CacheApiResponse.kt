package org.dbtools.kmp.commons.network.ktor

import io.ktor.http.HttpStatusCode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A response wrapper for HTTP API calls that support conditional caching via ETag and Last-Modified headers.
 *
 * This is similar to [ApiResponse], but designed for endpoints that support HTTP conditional requests
 * (If-None-Match / If-Modified-Since). It carries cache metadata ([Success.etag] and [Success.lastModified])
 * alongside the response data so callers can store and reuse them in subsequent requests.
 *
 * A [Success] is returned for both:
 * - **2xx responses**: [Success.data] contains the parsed response body, and [Success.etag]/[Success.lastModified]
 *   contain the cache headers from the response for use in future conditional requests.
 * - **304 Not Modified**: [Success.data] is `null` (the server confirmed the cached data is still valid),
 *   and [Success.etag]/[Success.lastModified] are populated from the response. Callers should continue
 *   using their previously cached data.
 *
 * A [Failure] is returned for error responses and exceptions:
 * - [Failure.Error.Forbidden]: 403 responses
 * - [Failure.Error.NoToken]: 480 responses
 * - [Failure.Error.Client]: Other 4xx responses (excluding 403 and 480)
 * - [Failure.Error.Server]: 5xx responses
 * - [Failure.Error.Unknown]: Any other non-success HTTP status
 * - [Failure.Exception]: An exception was thrown during the request (e.g. network failure)
 *
 * Use [HttpClient.executeSafelyCached][org.dbtools.kmp.commons.ext.executeSafelyCached] to execute an API call
 * and receive a [CacheApiResponse]. Use [cacheHeaders][org.dbtools.kmp.commons.ext.cacheHeaders] to attach
 * stored [etag][Success.etag] and [lastModified][Success.lastModified] values to subsequent requests.
 *
 * @param T The type of the successful response body.
 * @param E The type of the error details for client errors.
 */
sealed interface CacheApiResponse<T, E> {
    data class Success<T>(val data: T?, val etag: String?, val lastModified: String?) : CacheApiResponse<T, Nothing>
    sealed interface Failure<E> : CacheApiResponse<Nothing, E> {
        sealed interface Error<E> : Failure<E> {
            val message: String?

            data class Client<E>(val details: E?, override val message: String? = details?.toString()) : Error<E> // 4xx but not 403 or 480
            data class Forbidden(override val message: String?) : Error<Nothing> // 403
            data class NoToken(override val message: String?) : Error<Nothing> // 480
            data class Server(override val message: String?) : Error<Nothing> // 5xx
            data class Unknown(val status: HttpStatusCode, override val message: String?) : Error<Nothing> // Something else
        }

        data class Exception(val throwable: Throwable) : Failure<Nothing> {
            val message: String? = throwable.message
        }
    }
}

/**
 * Returns the encapsulated data if this instance represents [CacheApiResponse.Success] or
 * returns null if it is [CacheApiResponse.Failure.Error] or [CacheApiResponse.Failure.Exception].
 * @return The encapsulated data or null.
 */
fun <T> CacheApiResponse<out T, *>.getOrNull(): T? {
    return when (this) {
        is CacheApiResponse.Success -> data
        is CacheApiResponse.Failure -> null
    }
}

/**
 * Returns the encapsulated data if this instance represents [CacheApiResponse.Success] or
 * returns the [defaultValue] if it is [CacheApiResponse.Failure.Error] or [CacheApiResponse.Failure.Exception].
 * @return The encapsulated data or [defaultValue].
 */
fun <T> CacheApiResponse<out T, *>.getOrElse(defaultValue: T): T {
    return when (this) {
        is CacheApiResponse.Success -> data ?: defaultValue
        is CacheApiResponse.Failure -> defaultValue
    }
}

/**
 * Returns the encapsulated data if this instance represents [CacheApiResponse.Success] or
 * invokes the lambda [defaultValue] that returns [T] if it is [CacheApiResponse.Failure.Error] or [CacheApiResponse.Failure.Exception].
 *
 * @return The encapsulated data or [defaultValue].
 */
inline fun <T> CacheApiResponse<out T, *>.getOrElse(defaultValue: () -> T): T {
    return when (this) {
        is CacheApiResponse.Success -> data ?: defaultValue()
        is CacheApiResponse.Failure -> defaultValue()
    }
}

/**
 * Returns the encapsulated data if this instance represents [CacheApiResponse.Success] or
 * throws the encapsulated Throwable exception if it is [CacheApiResponse.Failure.Error] or [CacheApiResponse.Failure.Exception].
 *
 * @throws RuntimeException if it is [CacheApiResponse.Failure.Error] or
 * the encapsulated Throwable exception if it is [CacheApiResponse.Failure.Exception.throwable]
 *
 * @return The encapsulated data.
 */
fun <T> CacheApiResponse<out T, *>.getOrThrow(): T? {
    when (this) {
        is CacheApiResponse.Success -> return data
        is CacheApiResponse.Failure.Error -> error(message())
        is CacheApiResponse.Failure.Exception -> throw throwable
    }
}

/**
 *  Returns true if this instance represents an [CacheApiResponse.Success].
 */
inline val CacheApiResponse<*, *>.isSuccess: Boolean
    get() = this is CacheApiResponse.Success

/**
 *  Returns true if this instance represents an [CacheApiResponse.Failure].
 */
inline val CacheApiResponse<*, *>.isFailure: Boolean
    get() = this is CacheApiResponse.Failure

/**
 *  Returns true if this instance represents an [CacheApiResponse.Failure.Error].
 */
inline val CacheApiResponse<*, *>.isError: Boolean
    get() = this is CacheApiResponse.Failure.Error

/**
 *  Returns true if this instance represents an [CacheApiResponse.Failure.Exception].
 */
inline val CacheApiResponse<*, *>.isException: Boolean
    get() = this is CacheApiResponse.Failure.Exception

/**
 *  Returns The error message or null depending on the type of [CacheApiResponse].
 */
inline val CacheApiResponse<*, *>.messageOrNull: String?
    get() = when (this) {
        is CacheApiResponse.Failure.Error -> message
        is CacheApiResponse.Failure.Exception -> message
        else -> null
    }

/**
 * A scope function that would be executed for handling successful responses if the request succeeds.
 *
 * @param onResult The receiver function that receiving [CacheApiResponse.Success] if the request succeeds.
 * @return The original [CacheApiResponse].
 */
@Suppress("OutdatedDocumentation") // Detekt does not seem to work well with this function for documentation
@OptIn(ExperimentalContracts::class)
inline fun <T> CacheApiResponse<out T, *>.onSuccess(
    onResult: CacheApiResponse.Success<out T>.() -> Unit,
): CacheApiResponse<out T, *> {
    contract { callsInPlace(onResult, InvocationKind.AT_MOST_ONCE) }
    if (this is CacheApiResponse.Success) {
        onResult(this)
    }
    return this
}

/**
 * A function that would be executed for handling error responses if the request failed or get an exception.
 *
 * @param onResult The receiver function that receiving [CacheApiResponse.Failure] if the request failed or get an exception.
 * @return The original [CacheApiResponse].
 */
@Suppress("OutdatedDocumentation") // Detekt does not seem to work well with this function for documentation
@OptIn(ExperimentalContracts::class)
inline fun <T> CacheApiResponse<out T, *>.onFailure(
    onResult: CacheApiResponse.Failure<*>.() -> Unit,
): CacheApiResponse<out T, *> {
    contract { callsInPlace(onResult, InvocationKind.AT_MOST_ONCE) }
    if (this is CacheApiResponse.Failure) {
        onResult(this)
    }
    return this
}

/**
 * A scope function that would be executed for handling error responses if the request failed.
 *
 * @param onResult The receiver function that receiving [CacheApiResponse.Failure.Exception] if the request failed.
 * @return The original [CacheApiResponse].
 */
@Suppress("OutdatedDocumentation") // Detekt does not seem to work well with this function for documentation
@OptIn(ExperimentalContracts::class)
inline fun <E> CacheApiResponse<*, out E>.onError(
    onResult: CacheApiResponse.Failure.Error<out E>.() -> Unit,
): CacheApiResponse<*, out E> {
    contract { callsInPlace(onResult, InvocationKind.AT_MOST_ONCE) }
    if (this is CacheApiResponse.Failure.Error) {
        onResult(this)
    }
    return this
}

/**
 * A scope function that would be executed for handling exception responses if the request get an exception.
 *
 * @param onResult The receiver function that receiving [CacheApiResponse.Failure.Exception] if the request get an exception.
 * @return The original [CacheApiResponse].
 */
@Suppress("OutdatedDocumentation") // Detekt does not seem to work well with this function for documentation
@OptIn(ExperimentalContracts::class)
inline fun CacheApiResponse<*, *>.onException(
    onResult: CacheApiResponse.Failure.Exception.() -> Unit,
): CacheApiResponse<*, *> {
    contract { callsInPlace(onResult, InvocationKind.AT_MOST_ONCE) }
    if (this is CacheApiResponse.Failure.Exception) {
        onResult(this)
    }
    return this
}

/**
 * Returns an error message from the [CacheApiResponse.Failure] that consists of the localized message.
 * @return An error message from the [CacheApiResponse.Failure].
 */
fun CacheApiResponse.Failure<*>.message(): String {
    return when (this) {
        is CacheApiResponse.Failure.Error -> message()
        is CacheApiResponse.Failure.Exception -> message()
    }
}

/**
 * Returns an error message from the [CacheApiResponse.Failure.Error] that consists of the status and error response.
 * @return An error message from the [CacheApiResponse.Failure.Error].
 */
fun CacheApiResponse.Failure.Error<*>.message(): String = message ?: toString()

/**
 * Returns an error message from the [CacheApiResponse.Failure.Exception] that consists of the localized message.
 * @return An error message from the [CacheApiResponse.Failure.Exception].
 */
fun CacheApiResponse.Failure.Exception.message(): String = message ?: toString()
