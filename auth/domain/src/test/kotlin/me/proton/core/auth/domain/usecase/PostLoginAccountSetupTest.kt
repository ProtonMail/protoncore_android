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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.BillingDetails
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class PostLoginAccountSetupTest {
    private lateinit var accountWorkflowHandler: AccountWorkflowHandler
    private lateinit var performSubscribe: PerformSubscribe
    private lateinit var setupAccountCheck: SetupAccountCheck
    private lateinit var setupInternalAddress: SetupInternalAddress
    private lateinit var setupExternalAddressKeys: SetupExternalAddressKeys
    private lateinit var setupPrimaryKeys: SetupPrimaryKeys
    private lateinit var unlockUserPrimaryKey: UnlockUserPrimaryKey
    private lateinit var userCheck: PostLoginAccountSetup.UserCheck
    private lateinit var userManager: UserManager
    private lateinit var sessionManager: SessionManager
    private lateinit var user: User
    private lateinit var sessionId: SessionId
    private lateinit var onSetupSuccess: (suspend () -> Unit)

    private lateinit var tested: PostLoginAccountSetup

    private val testAccountType: AccountType = mockk()
    private val testEncryptedPassword = "encrypted-password"
    private val testUserId: UserId = UserId("user-id")

    @Before
    fun setUp() {
        accountWorkflowHandler = mockk()
        performSubscribe = mockk()
        setupAccountCheck = mockk()
        setupExternalAddressKeys = mockk()
        setupInternalAddress = mockk()
        setupPrimaryKeys = mockk()
        unlockUserPrimaryKey = mockk()
        onSetupSuccess = mockk { coJustRun { this@mockk.invoke() } }

        user = mockk()
        sessionId = mockk()
        userCheck = mockk {
            coEvery { this@mockk.invoke(any()) } returns PostLoginAccountSetup.UserCheckResult.Success
        }
        userManager = mockk {
            coEvery { getUser(any(), any()) } returns user
        }
        sessionManager = mockk {
            coEvery { getSessionId(any()) } returns sessionId
            coEvery { refreshScopes(any()) } returns emptyList()
        }

        tested = PostLoginAccountSetup(
            accountWorkflowHandler,
            performSubscribe,
            setupAccountCheck,
            setupExternalAddressKeys,
            setupInternalAddress,
            setupPrimaryKeys,
            unlockUserPrimaryKey,
            userCheck,
            userManager,
            sessionManager
        )
    }

    @Test
    fun `user unlocked`() = runBlockingTest {
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.UserUnlocked(testUserId), result)
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }
        coVerify(exactly = 1) { onSetupSuccess() }
    }

    @Test
    fun `user unlock failed, recoverable`() = runBlockingTest {
        val sessionInfo = mockSessionInfo()
        val unlockError = mockk<UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase>()

        coJustRun { accountWorkflowHandler.handleUnlockFailed(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns unlockError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )

        assertEquals(PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError(unlockError), result)
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `user unlock failed, non-recoverable `() = runBlockingTest {
        val sessionInfo = mockSessionInfo()
        val unlockError = mockk<UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey>()

        coJustRun { accountWorkflowHandler.handleUnlockFailed(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns unlockError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )

        assertEquals(PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError(unlockError), result)
        coVerify { accountWorkflowHandler.handleUnlockFailed(testUserId) }
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `needs second factor`() = runBlockingTest {
        val sessionInfo = mockSessionInfo(secondFactorNeeded = true)
        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.Need.SecondFactor(testUserId), result)
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `subscription setup with billing details`() = runBlockingTest {
        val sessionInfo = mockSessionInfo()
        val billingDetails = BillingDetails(
            amount = 99,
            currency = Currency.EUR,
            cycle = SubscriptionCycle.YEARLY,
            planName = "test-plan-name",
            token = ProtonPaymentToken("test-token"),
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coJustRun { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            billingDetails = billingDetails,
            onSetupSuccess = onSetupSuccess
        )

        assertEquals(PostLoginAccountSetup.Result.UserUnlocked(testUserId), result)
        coVerify(exactly = 1) { onSetupSuccess() }

        val userId = slot<UserId>()
        val amount = slot<Long>()
        val currency = slot<Currency>()
        val cycle = slot<SubscriptionCycle>()
        val planNames = slot<List<String>>()
        val paymentToken = slot<ProtonPaymentToken>()
        coVerify {
            performSubscribe.invoke(
                capture(userId),
                capture(amount),
                capture(currency),
                capture(cycle),
                capture(planNames),
                codes = null,
                paymentToken = capture(paymentToken),
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        assertEquals(testUserId, userId.captured)
        assertEquals(99, amount.captured)
        assertEquals(Currency.EUR, currency.captured)
        assertEquals(SubscriptionCycle.YEARLY, cycle.captured)
        assertEquals(listOf("test-plan-name"), planNames.captured)
        assertEquals(ProtonPaymentToken("test-token"), paymentToken.captured)
    }

    @Test
    fun `user check error`() = runBlockingTest {
        val setupError = mockk<PostLoginAccountSetup.UserCheckResult.Error>()
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountDisabled(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        coEvery { userCheck.invoke(any()) } returns setupError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertTrue(result is PostLoginAccountSetup.Result.Error.UserCheckError)
        coVerify { accountWorkflowHandler.handleAccountDisabled(testUserId) }
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `two pass needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.TwoPassNeeded
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleTwoPassModeNeeded(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.Need.TwoPassMode(testUserId), result)
        coVerify { accountWorkflowHandler.handleTwoPassModeNeeded(testUserId) }
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `change pass needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.ChangePasswordNeeded
        val sessionInfo = mockSessionInfo(temporaryPassword = true)

        coJustRun { accountWorkflowHandler.handleAccountDisabled(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.Need.ChangePassword(testUserId), result)
        coVerify { accountWorkflowHandler.handleAccountDisabled(testUserId) }
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `choose username needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.ChooseUsernameNeeded
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleCreateAddressNeeded(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.Need.ChooseUsername(testUserId), result)
        coVerify { accountWorkflowHandler.handleCreateAddressNeeded(testUserId) }
        coVerify(exactly = 0) { onSetupSuccess() }
    }

    @Test
    fun `primary keys needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.SetupPrimaryKeysNeeded
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError
        coJustRun { setupPrimaryKeys.invoke(any(), any(), any()) }
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.UserUnlocked(testUserId), result)
        coVerify { setupPrimaryKeys.invoke(testUserId, testEncryptedPassword, testAccountType) }
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }
        coVerify(exactly = 1) { onSetupSuccess() }
    }

    @Test
    fun `internal address needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.SetupInternalAddressNeeded
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError
        coJustRun { setupInternalAddress.invoke(any(), any()) }
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.UserUnlocked(testUserId), result)
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }
        coVerify { setupInternalAddress.invoke(testUserId) }
        coVerify(exactly = 1) { onSetupSuccess() }
    }

    @Test
    fun `external address needed`() = runBlockingTest {
        val setupError = SetupAccountCheck.Result.SetupExternalAddressKeysNeeded
        val sessionInfo = mockSessionInfo()

        coJustRun { accountWorkflowHandler.handleAccountReady(any()) }
        coEvery { setupAccountCheck.invoke(any(), any(), any(), any()) } returns setupError
        coJustRun { setupExternalAddressKeys.invoke(any()) }
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        val result = tested.invoke(
            sessionInfo.userId,
            testEncryptedPassword,
            testAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword,
            onSetupSuccess = onSetupSuccess
        )
        assertEquals(PostLoginAccountSetup.Result.UserUnlocked(testUserId), result)
        coVerify { accountWorkflowHandler.handleAccountReady(testUserId) }
        coVerify { setupExternalAddressKeys.invoke(testUserId) }
        coVerify(exactly = 1) { onSetupSuccess() }
    }

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
