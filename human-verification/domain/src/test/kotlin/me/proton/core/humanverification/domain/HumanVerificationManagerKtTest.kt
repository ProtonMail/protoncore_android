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

package me.proton.core.humanverification.domain

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationCancelled
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationFailed
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationInvalid
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationNeeded
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationSuccess
import kotlin.test.Test
import kotlin.test.assertEquals

class HumanVerificationManagerKtTest {
    @Test
    fun `filter out all states`() = runTest {
        val manager = mockk<HumanVerificationManager> {
            every { onHumanVerificationStateChanged(any()) } returns flowWithAllStates()
        }
        manager.onHumanVerificationState().test {
            awaitComplete()
        }
    }

    @Test
    fun `filter by single state`() = runTest {
        val manager = mockk<HumanVerificationManager> {
            every { onHumanVerificationStateChanged(any()) } returns flowWithAllStates()
        }
        manager.onHumanVerificationState(HumanVerificationSuccess).test {
            assertEquals(HumanVerificationSuccess, awaitItem().state)
            awaitComplete()
        }
    }

    private fun flowWithAllStates(): Flow<HumanVerificationDetails> = flowOf(
        mockHvDetails(HumanVerificationNeeded),
        mockHvDetails(HumanVerificationSuccess),
        mockHvDetails(HumanVerificationFailed),
        mockHvDetails(HumanVerificationCancelled),
        mockHvDetails(HumanVerificationInvalid)
    )

    private fun mockHvDetails(
        humanVerificationState: HumanVerificationState
    ): HumanVerificationDetails = mockk {
        every { state } returns humanVerificationState
    }
}
