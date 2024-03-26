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

package me.proton.core.featureflag.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.FeatureFlagWorkerManager
import me.proton.core.test.kotlin.UnconfinedTestCoroutineScopeProvider
import org.junit.Before
import org.junit.Test

class FeatureFlagRefreshStarterTest {

    private val userIdReady = UserId("ready")
    private val userIdDisabled = UserId("disabled")
    private val userIdRemoved = UserId("removed")
    private val accountReady = mockk<Account>(relaxed = true) {
        every { state } returns AccountState.Ready
        every { userId } returns userIdReady
    }
    private val accountDisabled = mockk<Account>(relaxed = true) {
        every { state } returns AccountState.Disabled
        every { userId } returns userIdDisabled
    }
    private val accountRemoved = mockk<Account>(relaxed = true) {
        every { state } returns AccountState.Removed
        every { userId } returns userIdRemoved
    }
    private val mutableAccount = MutableSharedFlow<Account>(replay = 1)

    private val workerManager = mockk<FeatureFlagWorkerManager>(relaxed = true)
    private val accountManager = mockk<AccountManager>(relaxed = true) {
        every { onAccountStateChanged(any()) } returns mutableAccount
    }

    private lateinit var starter: FeatureFlagRefreshStarter

    @Before
    fun setUp() {
        starter = FeatureFlagRefreshStarter(
            workerManager = workerManager,
            accountManager = accountManager,
            scopeProvider = UnconfinedTestCoroutineScopeProvider()
        )
    }

    @Test
    fun startCallEnqueueOneTimeForNull() = runTest {
        // When
        starter.start()
        // Then
        verify { workerManager.enqueueOneTime(null) }
    }

    @Test
    fun startCallEnqueuePeriodicOnAccountStateReady() = runTest {
        // Given
        starter.start()
        // When
        mutableAccount.emit(accountReady)
        // Then
        verify(exactly = 1) { workerManager.enqueuePeriodic(userIdReady, any()) }
        verify(exactly = 0) { workerManager.enqueuePeriodic(userIdDisabled, any()) }
    }

    @Test
    fun startCallCancelOnAccountStateDisabled() = runTest {
        // Given
        starter.start()
        // When
        mutableAccount.emit(accountDisabled)
        // Then
        verify(exactly = 0) { workerManager.cancel(userIdReady) }
        verify(exactly = 1) { workerManager.cancel(userIdDisabled) }
        verify(exactly = 0) { workerManager.cancel(userIdRemoved) }
    }

    @Test
    fun startCallCancelOnAccountStateRemoved() = runTest {
        // Given
        starter.start()
        // When
        mutableAccount.emit(accountRemoved)
        // Then
        verify(exactly = 0) { workerManager.cancel(userIdReady) }
        verify(exactly = 0) { workerManager.cancel(userIdDisabled) }
        verify(exactly = 1) { workerManager.cancel(userIdRemoved) }
    }

    @Test
    fun startCallEnqueuePeriodicOnInitialAccountStateReady() = runTest {
        // Given
        mutableAccount.emit(accountReady)
        // When
        starter.start()

        // Then
        verify(exactly = 1) { workerManager.enqueuePeriodic(userIdReady, any()) }
        verify(exactly = 0) { workerManager.enqueuePeriodic(userIdDisabled, any()) }
        verify(exactly = 0) { workerManager.enqueuePeriodic(userIdRemoved, any()) }
    }
}
