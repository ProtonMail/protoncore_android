/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.humanverification.data

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.observability.domain.ObservabilityManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HumanVerificationListenerImplTest {

    private lateinit var humanVerificationProvider: HumanVerificationProvider
    private lateinit var humanVerificationListener: HumanVerificationListener
    private lateinit var humanVerificationManager: HumanVerificationManager

    private val humanVerificationRepository = mockk<HumanVerificationRepository>()
    private val observabilityManager = mockk<ObservabilityManager>()

    private val session1 = Session(
        sessionId = SessionId("session1"),
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = listOf("full", "calendar", "mail")
    )

    private val clientId = ClientId.AccountSession(session1.sessionId)
    private val humanVerificationDetails = HumanVerificationDetails(
        clientId = clientId,
        verificationMethods = listOf(VerificationMethod.EMAIL),
        verificationToken = null,
        state = HumanVerificationState.HumanVerificationNeeded,
        tokenType = null,
        tokenCode = null
    )

    @Before
    fun beforeEveryTest() {
        humanVerificationProvider = HumanVerificationProviderImpl(humanVerificationRepository)
        humanVerificationListener = HumanVerificationListenerImpl(humanVerificationRepository)
        humanVerificationManager = HumanVerificationManagerImpl(
            humanVerificationProvider,
            humanVerificationListener,
            humanVerificationRepository,
            observabilityManager
        )

        coEvery { humanVerificationRepository.insertHumanVerificationDetails(any()) } returns Unit
    }

    @Test
    fun `on onHumanVerificationNeeded success`() = runTest {
        val humanVerificationApiDetails = HumanVerificationAvailableMethods(
            verificationMethods = listOf(VerificationMethod.EMAIL),
            verificationToken = "token"
        )

        coEvery { humanVerificationRepository.onHumanVerificationStateChanged(any()) } returns flowOf(
            humanVerificationDetails,
            humanVerificationDetails.copy(state = HumanVerificationState.HumanVerificationSuccess)
        )

        val result = humanVerificationListener.onHumanVerificationNeeded(clientId, humanVerificationApiDetails)

        val sessionStateLists = humanVerificationManager.onHumanVerificationStateChanged().toList()
        assertEquals(2, sessionStateLists.size)
        assertEquals(HumanVerificationListener.HumanVerificationResult.Success, result)
    }

    @Test
    fun `on onHumanVerificationNeeded failed`() = runTest {
        val humanVerificationApiDetails = HumanVerificationAvailableMethods(
            verificationMethods = listOf(VerificationMethod.EMAIL),
            verificationToken = "token"
        )

        coEvery { humanVerificationRepository.onHumanVerificationStateChanged(any()) } returns flowOf(
            humanVerificationDetails,
            humanVerificationDetails.copy(state = HumanVerificationState.HumanVerificationFailed)
        )

        val result = humanVerificationListener.onHumanVerificationNeeded(clientId, humanVerificationApiDetails)

        val sessionStateLists = humanVerificationManager.onHumanVerificationStateChanged().toList()
        assertEquals(2, sessionStateLists.size)

        assertEquals(HumanVerificationListener.HumanVerificationResult.Failure, result)
    }
}
