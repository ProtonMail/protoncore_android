/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.presentation.compose

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.user.domain.entity.User
import me.proton.core.userrecovery.data.usecase.DeleteRecoveryFiles
import me.proton.core.userrecovery.data.usecase.ObserveUserDeviceRecovery
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithInactiveKeysForRecovery
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithRecoverySecretButNoFile
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithoutRecoverySecret
import me.proton.core.userrecovery.data.usecase.StoreRecoveryFile
import me.proton.core.userrecovery.domain.usecase.GetRecoveryFile
import me.proton.core.userrecovery.domain.worker.UserRecoveryWorkerManager
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceRecoveryHandlerTest {
    @MockK
    private lateinit var deleteRecoveryFiles: DeleteRecoveryFiles

    @MockK
    private lateinit var getRecoveryFile: GetRecoveryFile

    @MockK
    private lateinit var observeUserDeviceRecovery: ObserveUserDeviceRecovery

    @MockK
    private lateinit var observeUsersWithInactiveKeysForRecovery: ObserveUsersWithInactiveKeysForRecovery

    @MockK
    private lateinit var observeUsersWithoutRecoverySecret: ObserveUsersWithoutRecoverySecret

    @MockK
    private lateinit var observeUsersWithRecoverySecretButNoFile: ObserveUsersWithRecoverySecretButNoFile

    @MockK
    private lateinit var storeRecoveryFile: StoreRecoveryFile

    @MockK
    private lateinit var userRecoveryWorkerManager: UserRecoveryWorkerManager

    private lateinit var testScopeProvider: TestCoroutineScopeProvider

    private lateinit var tested: DeviceRecoveryHandler

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        testScopeProvider = TestCoroutineScopeProvider()

        coJustRun { deleteRecoveryFiles(any()) }
        coJustRun { storeRecoveryFile(any(), any(), any()) }
        coJustRun { userRecoveryWorkerManager.enqueueSetRecoverySecret(any()) }

        tested = DeviceRecoveryHandler(
            testScopeProvider,
            deleteRecoveryFiles,
            getRecoveryFile,
            observeUserDeviceRecovery,
            observeUsersWithInactiveKeysForRecovery,
            observeUsersWithoutRecoverySecret,
            observeUsersWithRecoverySecretButNoFile,
            storeRecoveryFile,
            userRecoveryWorkerManager
        )
    }

    @Test
    fun `observers are started`() {
        // GIVEN
        val testUserId = UserId("user_id")
        val testUser = mockk<User> { every { userId } returns testUserId }

        coEvery { getRecoveryFile(testUserId) } returns GetRecoveryFile.Result(1, "recoveryFile")
        every { observeUsersWithInactiveKeysForRecovery() } returns MutableStateFlow(testUserId)
        every { observeUsersWithoutRecoverySecret() } returns MutableStateFlow(testUserId)
        every { observeUsersWithRecoverySecretButNoFile() } returns MutableStateFlow(testUserId)
        every { observeUserDeviceRecovery() } returns MutableStateFlow(Pair(testUser, false))

        // WHEN
        tested.start()
        testScopeProvider.GlobalDefaultSupervisedScope.testScheduler.runCurrent()

        // THEN
        coVerify { deleteRecoveryFiles(testUserId) }
        coVerify { userRecoveryWorkerManager.enqueueSetRecoverySecret(testUserId) }
        coVerify { userRecoveryWorkerManager.enqueueRecoverInactivePrivateKeys(testUserId) }
        coVerify { storeRecoveryFile("recoveryFile", 1, testUserId) }
    }
}
