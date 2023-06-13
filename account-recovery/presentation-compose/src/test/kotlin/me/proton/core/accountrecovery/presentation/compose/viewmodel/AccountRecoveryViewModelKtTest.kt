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

package me.proton.core.accountrecovery.presentation.compose.viewmodel

import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNull

internal class AccountRecoveryViewModelKtTest {


    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModelKt")
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModelKt")
    }

    @Test
    fun `account state dialog type mapping works correctly`() {
        var state = AccountRecoveryState.None
        var result = state.toDialogType()
        assertNull(result)

        state = AccountRecoveryState.GracePeriod
        result = state.toDialogType()
        assertEquals(AccountRecoveryDialogType.GRACE_PERIOD_STARTED, result)

        state = AccountRecoveryState.Cancelled
        result = state.toDialogType()
        assertEquals(AccountRecoveryDialogType.CANCELLATION_HAPPENED, result)

        state = AccountRecoveryState.ResetPassword
        result = state.toDialogType()
        assertEquals(AccountRecoveryDialogType.PASSWORD_CHANGE_PERIOD_STARTED, result)
    }
}
