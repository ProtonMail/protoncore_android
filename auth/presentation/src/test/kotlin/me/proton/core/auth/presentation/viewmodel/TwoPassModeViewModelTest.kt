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

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Dino Kadrikj.
 */
class TwoPassModeViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val unlockUserPrimaryKey = mockk<UnlockUserPrimaryKey>()
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)

    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPassword = "test-password"
    // endregion

    private lateinit var viewModel: TwoPassModeViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = TwoPassModeViewModel(accountManager, unlockUserPrimaryKey, keyStoreCrypto)
        every { keyStoreCrypto.decrypt(any<String>()) } returns testPassword
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPassword
    }

    @Test
    fun `mailbox login happy path`() = coroutinesTest {
        // GIVEN
        coEvery { unlockUserPrimaryKey.invoke(testUserId, testPassword) } returns UserManager.UnlockResult.Success
        viewModel.state.test {
            // WHEN
            viewModel.tryUnlockUser(testUserId, testPassword)

            // THEN
            assertIs<TwoPassModeViewModel.State.Idle>(expectItem())
            assertIs<TwoPassModeViewModel.State.Processing>(expectItem())
            assertIs<TwoPassModeViewModel.State.Success.UserUnLocked>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `success mailbox login invokes success on account manager`() = coroutinesTest {
        // GIVEN
        coEvery { unlockUserPrimaryKey.invoke(testUserId, testPassword) } returns UserManager.UnlockResult.Success
        // WHEN
        viewModel.tryUnlockUser(testUserId, testPassword)
        // THEN
        val arguments = slot<UserId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeSuccess(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeFailed(any()) }
        assertEquals(testUserId, arguments.captured)
    }

    @Test
    fun `stop mailbox login invokes failed on account manager`() = coroutinesTest {
        // WHEN
        viewModel.stopMailboxLoginFlow(testUserId)
        // THEN
        val arguments = slot<UserId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeFailed(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeSuccess(any()) }
    }
}
