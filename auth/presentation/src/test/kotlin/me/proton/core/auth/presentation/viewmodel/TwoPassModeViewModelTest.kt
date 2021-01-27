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

package me.proton.core.auth.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class TwoPassModeViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val unlockUserPrimaryKey = mockk<UnlockUserPrimaryKey>()
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testSessionId = SessionId("test-session-id")
    private val testPassword = "test-password"
    // endregion

    private lateinit var viewModel: TwoPassModeViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = TwoPassModeViewModel(accountManager, unlockUserPrimaryKey, sessionProvider)
        coEvery { sessionProvider.getUserId(any()) } returns testUserId
    }

    @Test
    fun `mailbox login happy path`() = coroutinesTest {
        // GIVEN
        coEvery {
            unlockUserPrimaryKey.invoke(
                testSessionId,
                testPassword.toByteArray()
            )
        } returns UserManager.UnlockResult.Success
        val observer = mockk<(TwoPassModeViewModel.State) -> Unit>(relaxed = true)
        viewModel.mailboxLoginState.observeDataForever(observer)
        // WHEN
        viewModel.tryUnlockUser(testSessionId, testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<TwoPassModeViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is TwoPassModeViewModel.State.Processing)
        assertTrue(successState is TwoPassModeViewModel.State.Success.UserUnLocked)
    }

    @Test
    fun `success mailbox login invokes success on account manager`() = coroutinesTest {
        // GIVEN
        coEvery {
            unlockUserPrimaryKey.invoke(
                testSessionId,
                testPassword.toByteArray()
            )
        } returns UserManager.UnlockResult.Success
        // WHEN
        viewModel.tryUnlockUser(testSessionId, testPassword.toByteArray())
        // THEN
        val arguments = slot<SessionId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeSuccess(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeFailed(any()) }
        assertEquals(testSessionId.id, arguments.captured.id)
    }

    @Test
    fun `stop mailbox login invokes failed on account manager`() = coroutinesTest {
        // WHEN
        viewModel.stopMailboxLoginFlow(testSessionId)
        // THEN
        val arguments = slot<SessionId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeFailed(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeSuccess(any()) }
    }
}
