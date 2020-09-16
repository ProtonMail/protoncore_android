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
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
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
class MyClassTest : CoroutinesTest {

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

    @get:Rule val coroutinesRule: CoroutinesTestRule
        get() = CoroutinesTestRule(dispatchers)

    val dispatchers: DispatcherProvider
        get() = TestDispatcherProvider

    /**
     * Use this for ensure that the test block is running on the provided dispatcher and avoid errors like
     * `Job has not completed yet`
     */
    fun coroutinesTest(
        context: CoroutineContext = dispatchers.Main,
        block: suspend TestCoroutineScope.() -> Unit
    ) = runBlockingTest(context, block)

    // TODO: remove in 0.2
    @Deprecated("Use from 'dispatchers'", ReplaceWith("dispatchers.Main"))
    val mainDispatcher get() = dispatchers.Main
    // TODO: remove in 0.2
    @Deprecated("Use from 'dispatchers'", ReplaceWith("dispatchers.Io"))
    val ioDispatcher get() = dispatchers.Io
    // TODO: remove in 0.2
    @Deprecated("Use from 'dispatchers'", ReplaceWith("dispatchers.Comp"))
    val compDispatcher get() = dispatchers.Comp
}

/** @see CoroutinesTest */
// TODO: remove in 0.2
@Deprecated(
    "Not needed anymore. One test can implement 'CoroutinesTest' without providing a concrete implementation"
)
val coroutinesTest = object : CoroutinesTest {}

/**
 * A JUnit Test Rule that set a Main Dispatcher
 * @author Davide Farella
 */
class CoroutinesTestRule internal constructor(
    val dispatchers: DispatcherProvider = TestDispatcherProvider
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatchers.Main)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}
