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

package me.proton.core.humanverification.presentation

import androidx.lifecycle.Lifecycle
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.onHumanVerificationState
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test

class HumanVerificationManagerObserverTest : ArchTest, CoroutinesTest {

    private lateinit var observer: HumanVerificationManagerObserver
    private val lifecycle = mockk<Lifecycle>()
    private val humanVerificationManager = mockk<HumanVerificationManager>(relaxed = true) {
        mockkStatic(HumanVerificationManager::onHumanVerificationState)
        coEvery { onHumanVerificationState(any(), any()) } returns flowOf(
            HumanVerificationDetails(
                clientId = ClientId.AccountSession(SessionId("some_session")),
                verificationMethods = emptyList(),
                state = HumanVerificationState.HumanVerificationSuccess,
            )
        )
    }

    @Before
    fun setup() {
        observer = HumanVerificationManagerObserver(
            lifecycle = lifecycle,
            humanVerificationManager = humanVerificationManager,
            scope = TestCoroutineScope(),
        )
    }

    @Test
    fun `cancelAllObservers cancels and removes all observers`() = coroutinesTest {
        // GIVEN
        val jobs = listOf(Job(), Job())
        observer.observerJobs.addAll(jobs)
        // WHEN
        observer.cancelAllObservers()
        // THEN
        assertTrue(observer.observerJobs.isEmpty())
        assertTrue(jobs.all { it.isCancelled })
    }

}
