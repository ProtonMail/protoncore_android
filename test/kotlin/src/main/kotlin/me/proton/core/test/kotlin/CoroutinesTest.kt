/*
 * Copyright (c) 2020 Proton Technologies AG
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

@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.proton.core.test.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.util.kotlin.DispatcherProvider
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.CoroutineContext

/**
 * An interface meant to be implemented by a Test Suite that uses Complex Concurrency via Coroutines.
 * Example:
```
class MyClassTest : CoroutinesTest by CoroutinesTest() {

@Test
fun `some test`() = coroutinesTest {
// testing structured concurrency here!
}
}
```
 *
 * It provides a [CoroutinesTestRule] and alternative dispatchers.
 *
 * @author Davide Farella
 */
interface CoroutinesTest {

    @get:Rule
    val coroutinesRule: CoroutinesTestRule

    /** Dispatchers used for a given test.
     * This property is available only during the test execution (after the test has started).
     */
    val dispatchers: DispatcherProvider get() = coroutinesRule.dispatchers

    /**
     * Use this for ensure that the test block is running on the provided dispatcher and avoid errors like
     * `Job has not completed yet`
     */
    fun coroutinesTest(
        context: CoroutineContext = coroutinesRule.dispatchers.Main,
        block: suspend TestScope.() -> Unit
    ) = runTest(context, testBody = block)
}

/** Helper for constructing a [CoroutinesTest]. */
fun CoroutinesTest(dispatchers: () -> DispatcherProvider = { TestDispatcherProvider() }): CoroutinesTest =
    object : CoroutinesTest {
        override val coroutinesRule: CoroutinesTestRule = CoroutinesTestRule(dispatchers)
    }

/** A [CoroutinesTest] that uses [UnconfinedTestDispatcher]. */
@Suppress("FunctionName")
fun UnconfinedCoroutinesTest(): CoroutinesTest = CoroutinesTest { TestDispatcherProvider(UnconfinedTestDispatcher()) }

/**
 * A JUnit Test Rule that set a Main Dispatcher
 * @author Davide Farella
 */
class CoroutinesTestRule internal constructor(
    val dispatchersFactory: () -> DispatcherProvider = { TestDispatcherProvider() }
) : TestWatcher() {
    lateinit var dispatchers: DispatcherProvider

    override fun starting(description: Description) {
        super.starting(description)
        dispatchers = dispatchersFactory()
        Dispatchers.setMain(dispatchers.Main)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}
