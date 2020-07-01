@file:Suppress("unused")

package me.proton.core.util.kotlin

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

/*
 * Set of utils related to Kotlin language
 * Author: Davide Farella
 */

/**
 * Await for the given [predicate] to return `true`
 * This 'awaiting' operation is executed on the [Default] Dispatcher
 */
suspend inline fun await(crossinline predicate: () -> Boolean) {
    withContext(Default) {
        while (!predicate()) { /** Await */ }
    }
}

/**
 * Executes [unsafeBlock] which could throw an exception.
 * @return `null` if exception is thrown
 */
inline fun <T> safe(unsafeBlock: () -> T) = try {
    unsafeBlock()
} catch (t: Throwable) {
    null
}

/**
 * Executes [unsafeBlock] which could throw an exception.
 * @return [default] if exception is thrown
 */
inline fun <T> safe(default: T, unsafeBlock: () -> T) = safe(unsafeBlock) ?: default

// region vararg's
/**
 * @return `true` if there is at least one item in [args] that matches the given [predicate]
 * Example: `` any(1, 2, 3) { it < 2 } `` -> true
 */
inline fun <T> any(vararg args: T, predicate: (T) -> Boolean) = args.any(predicate)

/**
 * @return `true` if there all the items in [args] match the given [predicate]
 * Example: `` all(1, 2, 3) { it < 4 } `` -> true
 */
inline fun <T> all(vararg args: T, predicate: (T) -> Boolean) = args.all(predicate)

/**
 * @return `true` if none of the items in [args] match the given [predicate]
 * Example: `` none(1, 2, 3) { it > 4 } `` -> true
 */
inline fun <T> none(vararg args: T, predicate: (T) -> Boolean) = args.all(predicate)

/**
 * Executes a [block] for every item in [args]
 * Example: `` forEach(1, 2, 3) { println(it) } ``
 */
inline fun <T> forEach(vararg args: T, block: (T) -> Unit) {
    args.forEach(block)
}
// endregion
