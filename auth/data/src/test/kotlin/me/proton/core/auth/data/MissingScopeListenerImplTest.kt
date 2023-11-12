/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.data

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MissingScopeListenerImplTest : CoroutinesTest by UnconfinedCoroutinesTest() {

    private lateinit var listener: MissingScopeListenerImpl

    @Before
    fun beforeEveryTest() {
        listener = MissingScopeListenerImpl()
    }

    @Test
    fun `onMissingScope failed`() = runTest {
        val job = launch(start = CoroutineStart.LAZY) {
            listener.onMissingScopeFailure()
        }
        listener.state.test {
            job.start()
            val result = listener.onMissingScope(UserId("test-user-id"), listOf(Scope.PASSWORD))
            val event = awaitItem()
            assertIs<MissingScopeState.ScopeMissing>(event)
            assertEquals(UserId("test-user-id"), (event as MissingScopeState.ScopeMissing).userId)
            assertEquals(listOf(Scope.PASSWORD), event.missingScopes)
            cancelAndIgnoreRemainingEvents()
            assertIs<MissingScopeResult.Failure>(result)
        }
    }

    @Test
    fun `onMissingScope success`() = runTest {
        val job = launch(start = CoroutineStart.LAZY) {
            listener.onMissingScopeSuccess()
        }
        listener.state.test {
            job.start()
            val result = listener.onMissingScope(UserId("test-user-id"), listOf(Scope.PASSWORD))
            val event = awaitItem()
            assertIs<MissingScopeState.ScopeMissing>(event)
            assertEquals(UserId("test-user-id"), (event as MissingScopeState.ScopeMissing).userId)
            assertEquals(listOf(Scope.PASSWORD), event.missingScopes)
            cancelAndIgnoreRemainingEvents()
            assertIs<MissingScopeResult.Success>(result)
        }
    }

    @Test
    fun `onMissingScopeSuccess`() = runTest {
        listener.state.test {
            listener.onMissingScopeSuccess()

            assertIs<MissingScopeState.ScopeObtainSuccess>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onMissingScopeFailure`() = runTest {
        listener.state.test {
            listener.onMissingScopeFailure()

            assertIs<MissingScopeState.ScopeObtainFailed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}