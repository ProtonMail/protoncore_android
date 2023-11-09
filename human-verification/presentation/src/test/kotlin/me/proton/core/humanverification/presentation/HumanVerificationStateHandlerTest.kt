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

package me.proton.core.humanverification.presentation

import android.app.Activity
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.ui.HumanVerificationActivity
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.test.android.ArchTest
import me.proton.core.test.android.lifecycle.TestLifecycle
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestDispatcherProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HumanVerificationStateHandlerTest : ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest({ TestDispatcherProvider(UnconfinedTestDispatcher()) }) {
    @MockK
    private lateinit var activityProvider: ActivityProvider

    @MockK
    private lateinit var appLifecycleObserver: AppLifecycleObserver

    @MockK
    private lateinit var humanVerificationManager: HumanVerificationManager

    private lateinit var tested: HumanVerificationStateHandler

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = HumanVerificationStateHandler(
            activityProvider,
            appLifecycleObserver,
            humanVerificationManager
        )
    }

    @Test
    fun `starts human verification workflow`() {
        // GIVEN
        val lifecycleOwner = TestLifecycle()
        val hvDetails = HumanVerificationDetails(
            clientId = ClientId.AccountSession(SessionId("session_id")),
            verificationMethods = emptyList(),
            verificationToken = "token",
            state = HumanVerificationState.HumanVerificationNeeded
        )
        val activity = mockk<Activity>(relaxed = true)
        every { activityProvider.lastResumed } returns activity
        every { appLifecycleObserver.lifecycle } returns lifecycleOwner.lifecycle
        every { humanVerificationManager.onHumanVerificationStateChanged(any()) } returns flowOf(
            hvDetails
        )

        // WHEN
        tested.observe()
        lifecycleOwner.create()

        // THEN
        val intentSlot = slot<Intent>()
        verify(exactly = 1) { activity.startActivityForResult(capture(intentSlot), any()) }
        assertEquals(
            HumanVerificationActivity::class.java.name,
            intentSlot.captured.component?.className
        )
    }
}
