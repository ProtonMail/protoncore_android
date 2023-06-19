/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.test.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.proton.core.util.kotlin.coroutine.ResultCollector
import me.proton.core.util.kotlin.coroutine.withResultContext
import okhttp3.internal.toImmutableList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/** Same as [kotlinx.coroutines.test.DEFAULT_DISPATCH_TIMEOUT_MS]. */
private const val DEFAULT_DISPATCH_TIMEOUT_MS = 60_000L

/** Executes [testBody] inside [runTest] and [withResultContext].
 * Example:
 * ```kotlin
 * runTestWithResultContext {
 *   runTestCase()
 *
 *   // obtain the results, that were recorded in `runTestCase`:
 *   assertNoResult("NoSuchKey")
 *   assertSingleResult("Cancellation")
 *   assertResults("SendEmail")
 * }
 * ```
 */
fun runTestWithResultContext(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatchTimeoutMs: Long = DEFAULT_DISPATCH_TIMEOUT_MS,
    testBody: suspend TestScopeWithResults.() -> Unit
) {
    runTest(context, dispatchTimeoutMs) {
        withResultContext {
            val scope = TestScopeWithResultsImpl(this, this@runTest)
            onResult { scope.record(it, this) }
            testBody(scope)
        }
    }
}

interface TestScopeWithResults : ResultCollector<Any?> {
    // region kotlinx.coroutines.test.TestScope
    val backgroundScope: CoroutineScope
    val testScheduler: TestCoroutineScheduler
    // endregion

    /** Asserts that there is no result for the given [key]. */
    fun assertNoResult(key: String)

    /** Returns a list of results for the given [key].
     * Throws an error if there are no results.
     */
    fun assertResults(key: String): List<Result<*>>

    /** Returns a single result for the given [key].
     * Throws an error if there are no results, or if there is more than one result.
     */
    fun assertSingleResult(key: String): Result<*>
}

private class TestScopeWithResultsImpl(
    private val resultCollector: ResultCollector<*>,
    private val testScope: TestScope
) : TestScopeWithResults, ResultCollector<Any?> {
    override val backgroundScope: CoroutineScope
        get() = testScope.backgroundScope

    override val testScheduler: TestCoroutineScheduler
        get() = testScope.testScheduler

    private val results = mutableMapOf<String, MutableList<Result<*>>>()

    override fun assertNoResult(key: String) {
        assertFalse(
            results.containsKey(key),
            "Expected no result for $key but got ${results[key]}."
        )
    }

    override fun assertResults(key: String): List<Result<*>> =
        assertNotNull(
            results[key]?.toImmutableList()?.takeIf { it.isNotEmpty() },
            "No result for $key."
        )

    override fun assertSingleResult(key: String): Result<*> =
        assertNotNull(results[key]?.takeIf { it.size == 1 }?.first(), "No result for $key.")

    override suspend fun onResult(key: String, action: suspend Result<Any?>.(key: String) -> Unit) {
        resultCollector.onResult(key, action)
    }

    fun record(key: String, result: Result<*>) {
        val results = results.getOrPut(key) { mutableListOf() }
        results.add(result)
    }
}
