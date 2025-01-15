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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.UnprivatizationInfo
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.usecase.sso.CheckDeviceSecret
import me.proton.core.auth.domain.usecase.sso.DecryptEncryptedSecret
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult.Error
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.PassphraseRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class PostLoginSsoAccountSetupTest {

    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var passphraseRepository: PassphraseRepository

    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var unlockUserPrimaryKey: UnlockUserPrimaryKey
    private lateinit var userCheck: PostLoginAccountSetup.UserCheck

    private lateinit var checkDeviceSecret: CheckDeviceSecret
    private lateinit var decryptEncryptedSecret: DecryptEncryptedSecret
    private lateinit var authDeviceRepository: AuthDeviceRepository

    private lateinit var user: User
    private lateinit var sessionId: SessionId

    private lateinit var tested: PostLoginSsoAccountSetup

    private val testUserId: UserId = UserId("user-id")
    private val passphrase = EncryptedByteArray(ByteArray(0))

    private val infoError = ApiException(
        Error.Http(
            httpCode = 400,
            message = "error",
            proton = Error.ProtonData(
                code = ResponseCodes.UNPRIVATIZATION_NOT_ALLOWED,
                error = "error"
            )
        )
    )
    private val infoSuccess = mockk<UnprivatizationInfo>()

    @Before
    fun setUp() {
        accountWorkflowHandler = mockk()

        user = mockk {
            every { this@mockk.flags } returns emptyMap()
            every { this@mockk.keys } returns emptyList()
        }
        sessionId = mockk()
        userCheck = mockk {
            coEvery { this@mockk.invoke(any()) } returns PostLoginAccountSetup.UserCheckResult.Success
        }
        unlockUserPrimaryKey = mockk {
            coEvery { this@mockk.invoke(any(), any<EncryptedString>()) } returns UserManager.UnlockResult.Success
        }
        userManager = mockk {
            coEvery { this@mockk.getUser(any(), any()) } returns user
            coEvery { this@mockk.unlockWithPassphrase(any(), any()) } returns UserManager.UnlockResult.Success
        }
        passphraseRepository = mockk {
            coEvery { this@mockk.getPassphrase(any()) } returns passphrase
        }
        sessionManager = mockk {
            coEvery { this@mockk.getSessionId(any()) } returns sessionId
            coEvery { this@mockk.refreshScopes(any()) } returns Unit
        }
        checkDeviceSecret = mockk {
            coEvery { this@mockk.invoke(any()) } returns null
        }
        decryptEncryptedSecret = mockk {
            coEvery { this@mockk.invoke(any(), any()) } returns null
        }
        authDeviceRepository = mockk {
            coEvery { this@mockk.getUnprivatizationInfo(any<UserId>()) } throws infoError
        }
    }

    @Test
    fun `user check error`() = runTest {
        tested = mockTested()

        val setupError = mockk<PostLoginAccountSetup.UserCheckResult.Error>()
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountDisabled(any()) }
        coEvery { userCheck.invoke(any()) } returns setupError

        val result = tested.invoke(sessionInfo.userId)
        assertTrue(result is PostLoginAccountSetup.Result.Error)
        coVerify { accountWorkflowHandler.handleAccountDisabled(testUserId) }
    }

    @Test
    fun `user check success`() = runTest {
        tested = mockTested()

        val setupSuccess = mockk<PostLoginAccountSetup.UserCheckResult.Success>()
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { userCheck.invoke(any()) } returns setupSuccess

        val result = tested.invoke(sessionInfo.userId)
        assertTrue(result is PostLoginAccountSetup.Result.AccountReady)
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }
    }

    @Test
    fun `verify check success start device secret workflow`() = runTest {
        coEvery { authDeviceRepository.getUnprivatizationInfo(any<UserId>()) } returns infoSuccess
        coEvery { passphraseRepository.getPassphrase(any()) } returns null

        tested = mockTested()

        val sessionInfo = mockSessionInfo()
        coJustRun { accountWorkflowHandler.handleDeviceSecretNeeded(any()) }

        val result = tested.invoke(sessionInfo.userId)
        assertTrue(result is PostLoginAccountSetup.Result.Need.DeviceSecret)
        coVerify { accountWorkflowHandler.handleDeviceSecretNeeded(testUserId) }
    }

    private fun mockTested() = PostLoginSsoAccountSetup(
        accountWorkflow = accountWorkflowHandler,
        userCheck = userCheck,
        userManager = userManager,
        sessionManager = sessionManager,
        passphraseRepository = passphraseRepository,
        checkDeviceSecret = checkDeviceSecret,
        decryptEncryptedSecret = decryptEncryptedSecret,
        authDeviceRepository = authDeviceRepository
    )

    private fun mockSessionInfo(
        secondFactorNeeded: Boolean = false,
        temporaryPassword: Boolean = false
    ) = mockk<SessionInfo> {
        every { userId } returns testUserId
        every { isSecondFactorNeeded } returns secondFactorNeeded
        every { isTwoPassModeNeeded } returns false
        every { this@mockk.temporaryPassword } returns temporaryPassword
    }
}
