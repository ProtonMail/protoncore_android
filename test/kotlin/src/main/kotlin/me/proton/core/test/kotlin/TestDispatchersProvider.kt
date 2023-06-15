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

package me.proton.core.test.kotlin

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider

/**
 * Implementation of [DispatcherProvider] meant to be used for tests.
 */
class TestDispatcherProvider(
    mainDispatcher: TestDispatcher,
    ioDispatcher: TestDispatcher,
    compDispatcher: TestDispatcher,
) : DispatcherProvider {
    override val Main: TestDispatcher = mainDispatcher
    override val Io: TestDispatcher = ioDispatcher
    override val Comp: TestDispatcher = compDispatcher

    constructor(dispatcher: TestDispatcher = StandardTestDispatcher()) : this(
        dispatcher,
        dispatcher,
        dispatcher
    )
}

/**
 * [CoroutineScopeProvider] for tests, using [StandardTestDispatcher].
 */
open class TestCoroutineScopeProvider(
    dispatcherProvider: DispatcherProvider = TestDispatcherProvider()
) : CoroutineScopeProvider {
    override val GlobalDefaultSupervisedScope = TestScope(dispatcherProvider.Comp + SupervisorJob())
    override val GlobalIOSupervisedScope = TestScope(dispatcherProvider.Io + SupervisorJob())
}

/**
 * [CoroutineScopeProvider] for tests, using [UnconfinedTestDispatcher].
 */
class UnconfinedTestCoroutineScopeProvider : TestCoroutineScopeProvider(
    TestDispatcherProvider(UnconfinedTestDispatcher())
)
