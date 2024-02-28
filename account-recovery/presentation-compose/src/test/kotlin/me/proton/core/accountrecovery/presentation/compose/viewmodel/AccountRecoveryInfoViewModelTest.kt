/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.accountrecovery.presentation.compose.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.DateTimeFormat
import me.proton.core.util.android.datetime.DurationFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.hours

@RunWith(RobolectricTestRunner::class)
internal class AccountRecoveryInfoViewModelTest {

    private val userId = UserId("userId")

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val accountManager: AccountManager = mockk {
        coEvery { getPrimaryUserId() } returns flowOf(userId)
    }
    private val observeUserRecovery: ObserveUserRecovery = mockk()
    private val clock: Clock = Clock.fixed(0)

    private lateinit var viewModel: AccountRecoveryInfoViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = AccountRecoveryInfoViewModel(
            accountManager = accountManager,
            observeUserRecovery = observeUserRecovery,
            clock = clock,
            dateTimeFormat = DateTimeFormat(context),
            durationFormat = DurationFormat(context)
        )
    }

    @Test
    fun userRecoveryStateNull() = runTest {
        // GIVEN
        every { observeUserRecovery.invoke(any()) } returns flowOf(null)

        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryInfoViewState.None>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userRecoveryStateNone() = runTest {
        // GIVEN
        every { observeUserRecovery.invoke(any()) } returns flowOf(makeUserRecovery(UserRecovery.State.None))

        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryInfoViewState.None>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userRecoveryStateRecovery() = runTest {
        // GIVEN
        every { observeUserRecovery.invoke(any()) } returns flowOf(
            makeUserRecovery(UserRecovery.State.Grace)
        )
        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryInfoViewState.Recovery>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun makeUserRecovery(state: UserRecovery.State): UserRecovery = UserRecovery(
        state = IntEnum(state.value, state),
        startTime = clock.currentEpochSeconds(),
        endTime = clock.currentEpochSeconds() + 24.hours.inWholeSeconds,
        sessionId = SessionId("session_id"),
        reason = null
    )
}
