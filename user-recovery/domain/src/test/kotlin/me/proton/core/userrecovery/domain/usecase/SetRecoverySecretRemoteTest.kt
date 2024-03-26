/*
 * Copyright (c) 2024 ProtonTechnologies AG
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

package me.proton.core.userrecovery.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import me.proton.core.usersettings.domain.repository.UserSettingsRemoteDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test

class SetRecoverySecretRemoteTest {

    private val userId = UserId("test")
    private val secret = "secret"
    private val signature = "signature"

    private val eventManagerProvider: EventManagerProvider = mockk(relaxed = true) {
        coEvery { this@mockk.suspend(any(), captureLambda<suspend () -> Unit>()) } coAnswers {
            lambda<(suspend () -> Unit)>().captured()
        }
    }
    private val getRecoverySecret: GetRecoverySecret = mockk(relaxed = true) {
        coEvery { this@mockk.invoke(any()) } returns Pair(secret, signature)
    }
    private val userSettingsRemoteDataSource: UserSettingsRemoteDataSource = mockk(relaxed = true)

    private lateinit var tested: SetRecoverySecretRemote

    @Before
    fun setup() {
        mockkStatic("me.proton.core.eventmanager.domain.extension.EventManagerKt")
        tested = SetRecoverySecretRemote(eventManagerProvider, getRecoverySecret, userSettingsRemoteDataSource)
    }

    @After
    fun tearDown() {
        unmockkStatic("me.proton.core.eventmanager.domain.extension.EventManagerKt")
    }

    @Test
    fun suspendIsCalled() = runTest {
        // WHEN
        tested.invoke(userId)
        // THEN
        coVerify { eventManagerProvider.suspend(EventManagerConfig.Core(userId), any()) }
    }

    @Test
    fun setRecoverySecretIsCalled() = runTest {
        // WHEN
        tested.invoke(userId)
        // THEN
        coVerify { userSettingsRemoteDataSource.setRecoverySecret(userId, secret, signature) }
    }
}
