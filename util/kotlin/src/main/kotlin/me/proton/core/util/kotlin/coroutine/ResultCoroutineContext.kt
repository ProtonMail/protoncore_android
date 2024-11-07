/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.util.kotlin.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Calls the specified suspending block with a [coroutineContext] + [ResultCoroutineContextElement],
 * suspends until it completes, and returns the result.
 *
 * Example of usage:
 *
 * ```
 * ...
 * suspend fun foo() {
 *     bar()
 * }
 * ...
 * suspend fun bar() {
 *     result("key") { ... }
 * }
 * ...
 * withResultContext {
 *     onResult("key") { ... }
 *     onComplete { ... }
 *
 *     foo()
 * }
 * ```
 * @see [result]
 * @see [ResultCollector]
 */
suspend fun <T> withResultContext(
    allowDuplicateResultKey: Boolean = true,
    block: suspend ResultCollector<*>.() -> T,
): T {
    val resultContext = coroutineContext[ResultCoroutineContextElement] ?: ResultCoroutineContextElement()
    val resultCollector = ResultCollector { key, onResult ->
        resultContext.addObserver(allowDuplicateResultKey, key, onResult)
    }
    return withContext(coroutineContext + resultContext) {
        result(resultCollector.key()) { block(resultCollector) }
    }
}

/**
 * Set the [Result] from the specified suspending [block] for the given [key].
 *
 * @see [withResultContext]
 * @see [ResultCollector.onResult]
 */
suspend fun <T> result(
    key: String,
    block: suspend () -> T
): T = runCatching { block() }
    .also { result -> coroutineContext[ResultCoroutineContextElement]?.invokeObservers(key, result) }
    .getOrThrow()

// Key for observing/matching any key/result.
internal const val KEY_ANY = "*"

/**
 * Class retaining observers of [ResultCollector.onResult] in a [CoroutineContext].
 *
 * @see [withResultContext]
 * @see [result]
 */
internal class ResultCoroutineContextElement : AbstractCoroutineContextElement(Key) {

    private val observerMap = ConcurrentHashMap<String, MutableList<suspend Result<*>.(key: String) -> Unit>>()

    fun addObserver(allowDuplicateKey: Boolean, key: String, onResult: suspend Result<*>.(key: String) -> Unit) {
        check(allowDuplicateKey || !observerMap.containsKey(key)) { "Duplicate Result key not allowed." }
        observerMap.getOrPut(key) { mutableListOf() }.add(onResult)
    }

    suspend fun invokeObservers(key: String, value: Result<*>) {
        observerMap.getOrPut(key) { mutableListOf() }.forEach { it.invoke(value, key) }
        observerMap.getOrPut(KEY_ANY) { mutableListOf() }.forEach { it.invoke(value, key) }
    }

    companion object Key : CoroutineContext.Key<ResultCoroutineContextElement>
}

/**
 * [ResultCollector] is used as an intermediate collector of [Result] within a [withResultContext].
 *
 * Example of usage:
 *
 * ```
 * withResultContext { this: ResultCollector<*>
 *     onResult("key") { ... }
 *     onComplete { ... }
 *     ...
 * }
 * ```
 */
fun interface ResultCollector<T> {

    /**
     * Unique key to identify this instance.
     */
    fun key(): String = hashCode().toString()

    /**
     * Performs the given [action] when [result] with the given [key] is called.
     */
    suspend fun onResult(key: String, action: suspend Result<T>.(key: String) -> Unit)

    /**
     * Performs the given [action] when any [result] is called.
     *
     * Note: This is also called for the result of [withResultContext] block using [ResultCollector.key].
     */
    suspend fun onResult(action: suspend Result<T>.(key: String) -> Unit) = onResult(KEY_ANY, action)

    /**
     * Performs the given [action] when the [withResultContext] block complete.
     */
    suspend fun onComplete(action: suspend Result<T>.(key: String) -> Unit) = onResult(key(), action)
}

/**
 * Performs the given [action] when [result] with the given [key] is called, wrapping [action] using [runCatching].
 */
suspend fun <T> ResultCollector<T>.onResultCatching(key: String, action: suspend Result<T>.(key: String) -> Unit) =
    onResult(key) { runCatching { action(it) } }

/**
 * Performs the given [action] when any [result] is called, wrapping [action] using [runCatching].
 */
suspend fun <T> ResultCollector<T>.onResultCatching(action: suspend Result<T>.(key: String) -> Unit) =
    onResult { runCatching { action(it) } }

/**
 * Performs the given [action] when the [withResultContext] block complete, wrapping [action] using [runCatching].
 */
suspend fun <T> ResultCollector<T>.onCompleteCatching(action: suspend Result<T>.() -> Unit) =
    onComplete { runCatching { action() } }

/**
 * Performs the given [action] when the [withResultContext] returned value represents [success][Result.isSuccess].
 */
suspend fun <T> ResultCollector<T>.onSuccess(action: suspend Result<T>.() -> Unit) =
    onComplete { onSuccess { action() } }

/**
 * Performs the given [action] when the [withResultContext] returned value represents [failure][Result.isFailure].
 */
suspend fun <T> ResultCollector<T>.onFailure(action: suspend Result<T>.() -> Unit) =
    onComplete { onFailure { action() } }

/**
 * Launches a new coroutine without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is wrapped with a [ResultCoroutineContextElement] using [withResultContext].
 *
 * @see [withResultContext]
 * @see [result]
 */
fun <T> CoroutineScope.launchWithResultContext(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend ResultCollector<*>.() -> T
): Job = launch(context, start) { withResultContext(allowDuplicateResultKey = true, block) }

/**
 * [FlowResultCollector] is used as collector of the [Flow] and [Result].
 *
 * @see [FlowCollector]
 * @see [ResultCollector]
 * @see [flowWithResultContext]
 */
class FlowResultCollector<T>(
    private val producerScope: ProducerScope<T>,
    private val resultCollector: ResultCollector<T>
) : FlowCollector<T>, ResultCollector<T> {

    override fun key(): String = resultCollector.key()

    override suspend fun emit(value: T) {
        producerScope.send(value)
    }

    override suspend fun onResult(key: String, action: suspend Result<T>.(key: String) -> Unit) {
        resultCollector.onResult(key, action)
    }
}

/**
 * Creates a cold flow from the given suspendable block.
 *
 * The provided [FlowResultCollector] can be used to as a [FlowCollector] and [ResultCollector].
 *
 * Note: Allowing duplicate [result] key can lead to undesirable side-effect.
 *
 * @see [FlowResultCollector]
 * @see [withResultContext]
 */
fun <T> flowWithResultContext(
    allowDuplicateResultKey: Boolean = false,
    block: suspend FlowResultCollector<T>.() -> Unit
): Flow<T> = channelFlow {
    val producerScope = this
    withResultContext(allowDuplicateResultKey) {
        val resultCollector = this as ResultCollector<T>
        val collector = FlowResultCollector(producerScope, resultCollector)
        block(collector)
    }
}
