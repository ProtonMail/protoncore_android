package me.proton.core.util.kotlin

import kotlinx.coroutines.delay

/**
 * Retry a function [block] on error the given number of [times] if [predicate] return true.
 */
suspend fun <T : Any> retry(
    times: Int = 2,
    delayMillis: Long = 1000,
    predicate: (Throwable) -> Boolean = { true },
    block: suspend () -> T
): T = runCatching { block() }.getOrElse {
    if (times == 0 || !predicate(it)) throw it
    delay(delayMillis)
    retry(times - 1, delayMillis, predicate, block)
}
