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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineDispatcher
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider

/**
 * Implementation of [DispatcherProvider] meant to be used for tests
 */
object TestDispatcherProvider : DispatcherProvider {
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    override val Main = testCoroutineDispatcher
    override val Io = testCoroutineDispatcher
    override val Comp = testCoroutineDispatcher

    fun cleanupTestCoroutines() {
        testCoroutineDispatcher.cleanupTestCoroutines()
    }
}

object TestCoroutineScopeProvider : CoroutineScopeProvider {
    override val GlobalDefaultSupervisedScope = CoroutineScope(TestDispatcherProvider.Comp + SupervisorJob())
    override val GlobalIOSupervisedScope = CoroutineScope(TestDispatcherProvider.Io + SupervisorJob())
}
