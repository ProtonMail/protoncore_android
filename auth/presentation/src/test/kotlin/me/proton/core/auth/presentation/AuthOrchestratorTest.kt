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

package me.proton.core.auth.presentation

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.auth.domain.feature.IsLoginTwoStepEnabled
import me.proton.core.auth.presentation.alert.confirmpass.StartConfirmPassword
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.ui.StartAddAccount
import me.proton.core.auth.presentation.ui.StartChooseAddress
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartLoginSso
import me.proton.core.auth.presentation.ui.StartLoginTwoStep
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartSignup
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthOrchestratorTest {

    private val context = mockk<Context>(relaxed = true)

    private val isLoginTwoStepEnabled = mockk<IsLoginTwoStepEnabled> {
        every { this@mockk.invoke() } returns false
    }

    private val addAccountLauncher = mockk<ActivityResultLauncher<AddAccountInput>>(relaxed = true)
    private val loginLauncher = mockk<ActivityResultLauncher<LoginInput>>(relaxed = true)
    private val loginTwoStepLauncher = mockk<ActivityResultLauncher<LoginInput>>(relaxed = true)
    private val loginSsoLauncher = mockk<ActivityResultLauncher<LoginSsoInput>>(relaxed = true)
    private val secondFactorLauncher = mockk<ActivityResultLauncher<SecondFactorInput>>(relaxed = true)
    private val twoPassModeLauncher = mockk<ActivityResultLauncher<TwoPassModeInput>>(relaxed = true)
    private val chooseAddressLauncher = mockk<ActivityResultLauncher<ChooseAddressInput>>(relaxed = true)
    private val signUpLauncher = mockk<ActivityResultLauncher<SignUpInput>>(relaxed = true)

    private val caller = mockk<ActivityResultCaller>(relaxed = true) {
        every { registerForActivityResult(StartAddAccount, any()) } returns addAccountLauncher
        every { registerForActivityResult(StartLogin, any()) } returns loginLauncher
        every { registerForActivityResult(StartLoginTwoStep, any()) } returns loginTwoStepLauncher
        every { registerForActivityResult(StartLoginSso, any()) } returns loginSsoLauncher
        every { registerForActivityResult(StartSecondFactor, any()) } returns secondFactorLauncher
        every { registerForActivityResult(StartTwoPassMode, any()) } returns twoPassModeLauncher
        every { registerForActivityResult(StartChooseAddress, any()) } returns chooseAddressLauncher
        every { registerForActivityResult(StartSignup, any()) } returns signUpLauncher
    }

    private lateinit var orchestrator: AuthOrchestrator

    @Before
    fun beforeEveryTest() {
        orchestrator = AuthOrchestrator(context, isLoginTwoStepEnabled)
    }

    @Test
    fun registerLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        // Then
        verify { caller.registerForActivityResult(any<StartAddAccount>(), any()) }
        verify { caller.registerForActivityResult(any<StartLogin>(), any()) }
        verify { caller.registerForActivityResult(any<StartLoginSso>(), any()) }
        verify { caller.registerForActivityResult(any<StartSecondFactor>(), any()) }
        verify { caller.registerForActivityResult(any<StartTwoPassMode>(), any()) }
        verify { caller.registerForActivityResult(any<StartChooseAddress>(), any()) }
        verify { caller.registerForActivityResult(any<StartSignup>(), any()) }
        verify { caller.registerForActivityResult(any<StartConfirmPassword>(), any()) }
    }

    @Test
    fun unregisterLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        orchestrator.unregister()
        // Then
        verify { addAccountLauncher.unregister() }
        verify { loginLauncher.unregister() }
        verify { secondFactorLauncher.unregister() }
        verify { twoPassModeLauncher.unregister() }
        verify { chooseAddressLauncher.unregister() }
        verify { signUpLauncher.unregister() }
    }

    @Test
    fun startAddAccountWorkflow() = runTest {
        // Given
        orchestrator.register(caller)
        val input = AddAccountInput(null)
        // When
        orchestrator.startAddAccountWorkflow()
        // Then
        verify(exactly = 1) { addAccountLauncher.launch(input) }
    }

    @Test
    fun startLoginWorkflow() = runTest {
        // Given
        orchestrator.register(caller)
        val username = "test-username"
        val input = LoginInput(username)
        // When
        orchestrator.startLoginWorkflow(username)
        // Then
        verify(exactly = 1) { loginLauncher.launch(input) }
    }

    @Test
    fun startLoginWorkflow_LoginTwoStep() = runTest {
        // Given
        every { isLoginTwoStepEnabled.invoke() } returns true

        orchestrator.register(caller)
        val username = "test-username"
        val input = LoginInput(username)
        // When
        orchestrator.startLoginWorkflow(username)
        // Then
        verify(exactly = 1) { loginTwoStepLauncher.launch(input) }
        verify(exactly = 0) { loginLauncher.launch(input) }
    }

    @Test
    fun `startLoginWorkflow username null`() = runTest {
        // Given
        orchestrator.register(caller)
        val input = LoginInput(null)
        // When
        orchestrator.startLoginWorkflow(null)
        // Then
        verify(exactly = 1) { loginLauncher.launch(input) }
    }

    @Test
    fun `startLoginWorkflow default username password`() = runTest {
        // Given
        orchestrator.register(caller)
        val input = LoginInput()
        // When
        orchestrator.startLoginWorkflow()
        // Then
        verify(exactly = 1) { loginLauncher.launch(input) }
    }

    @Test
    fun `startSecondFactorWorkflow account session null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        every { account.details } returns accountDetails
        every { accountDetails.session } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startSecondFactorWorkflow(account)
        }.message
        assertEquals("Required AccountType is null for startSecondFactorWorkflow.", message)
    }

    @Test
    fun `startSecondFactorWorkflow password null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        val session = mockk<SessionDetails>(relaxed = true)
        every { account.details } returns accountDetails
        every { accountDetails.session } returns session
        every { session.password } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startSecondFactorWorkflow(account)
        }.message
        assertEquals("Password is null for startSecondFactorWorkflow.", message)
    }

    @Test
    fun `startSecondFactorWorkflow`() = runTest {
        // Given
        orchestrator.register(caller)
        val userId = UserId("test-user-id")
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        val session = mockk<SessionDetails>(relaxed = true)
        val encryptedPassword = "test-password-encrypted"
        every { account.details } returns accountDetails
        every { account.userId } returns userId
        every { accountDetails.session } returns session
        every { session.requiredAccountType } returns AccountType.Internal
        every { session.password } returns encryptedPassword
        every { session.twoPassModeEnabled } returns true
        // When
        val input = SecondFactorInput(userId.id, encryptedPassword, AccountType.Internal, true)
        orchestrator.startSecondFactorWorkflow(account)
        // Then
        verify(exactly = 1) { secondFactorLauncher.launch(input) }
    }

    @Test
    fun `startTwoPassModeWorkflow session null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        every { account.details } returns accountDetails
        every { accountDetails.session } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startTwoPassModeWorkflow(account)
        }.message
        // Then
        assertEquals("Required AccountType is null for startSecondFactorWorkflow.", message)
    }

    @Test
    fun `startTwoPassModeWorkflow`() = runTest {
        // Given
        orchestrator.register(caller)
        val userId = UserId("test-user-id")
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        val session = mockk<SessionDetails>(relaxed = true)
        val encryptedPassword = "test-password-encrypted"
        every { account.details } returns accountDetails
        every { account.userId } returns userId
        every { accountDetails.session } returns session
        every { session.requiredAccountType } returns AccountType.Internal
        every { session.password } returns encryptedPassword
        every { session.twoPassModeEnabled } returns true
        // When
        val input = TwoPassModeInput(userId.id, AccountType.Internal)
        orchestrator.startTwoPassModeWorkflow(account)
        // Then
        verify(exactly = 1) { twoPassModeLauncher.launch(input) }
    }

    @Test
    fun `startChooseAddressWorkflow session null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        every { account.email } returns "test-email"
        every { account.details } returns accountDetails
        every { accountDetails.session } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startChooseAddressWorkflow(account)
        }.message
        // Then
        assertEquals("Password is null for startChooseAddressWorkflow.", message)
    }

    @Test
    fun `startChooseAddressWorkflow password null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        val session = mockk<SessionDetails>(relaxed = true)
        every { account.email } returns "test-email"
        every { account.details } returns accountDetails
        every { accountDetails.session } returns session
        every { session.password } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startChooseAddressWorkflow(account)
        }.message
        // Then
        assertEquals("Password is null for startChooseAddressWorkflow.", message)
    }

    @Test
    fun `startChooseAddressWorkflow email null`() = runTest {
        // Given
        orchestrator.register(caller)
        val account = mockk<Account>(relaxed = true)
        every { account.email } returns null
        // When
        val message = assertFailsWith<IllegalStateException> {
            orchestrator.startChooseAddressWorkflow(account)
        }.message
        // Then
        assertEquals("Email is null for startChooseAddressWorkflow.", message)
    }

    @Test
    fun `startChooseAddressWorkflow`() = runTest {
        // Given
        orchestrator.register(caller)
        val userId = UserId("test-user-id")
        val account = mockk<Account>(relaxed = true)
        val accountDetails = mockk<AccountDetails>(relaxed = true)
        val session = mockk<SessionDetails>(relaxed = true)
        val encryptedPassword = "test-password-encrypted"
        val email = "test-email"
        every { account.email } returns email
        every { account.details } returns accountDetails
        every { account.userId } returns userId
        every { accountDetails.session } returns session
        every { session.requiredAccountType } returns AccountType.Internal
        every { session.password } returns encryptedPassword
        every { session.twoPassModeEnabled } returns true
        // When
        val input = ChooseAddressInput(userId.id, encryptedPassword, email, true)
        orchestrator.startChooseAddressWorkflow(account)
        // Then
        verify(exactly = 1) { chooseAddressLauncher.launch(input) }
    }

    @Test
    fun `startSignupWorkflow`() = runTest {
        // Given
        orchestrator.register(caller)
        val input = SignUpInput()
        // When
        orchestrator.startSignupWorkflow()
        // Then
        verify(exactly = 1) { signUpLauncher.launch(input) }
    }

    @Test
    fun `startSignupWorkflow default`() = runTest {
        // Given
        orchestrator.register(caller)
        val input = SignUpInput()
        // When
        orchestrator.startSignupWorkflow()
        // Then
        verify(exactly = 1) { signUpLauncher.launch(input) }
    }

    @Test
    fun `onAddAccountResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onAddAccountResult(mockk())
        verify { orchestrator.setOnAddAccountResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }

    @Test
    fun `onOnSignUpResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onOnSignUpResult(mockk())
        verify { orchestrator.setOnSignUpResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }

    @Test
    fun `onLoginResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onLoginResult(mockk())
        verify { orchestrator.setOnLoginResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }

    @Test
    fun `onTwoPassModeResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onTwoPassModeResult(mockk())
        verify { orchestrator.setOnTwoPassModeResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }

    @Test
    fun `onSecondFactorResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onSecondFactorResult(mockk())
        verify { orchestrator.setOnSecondFactorResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }

    @Test
    fun `onChooseAddressResult`() = runTest {
        mockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
        val orchestrator = mockk<AuthOrchestrator>(relaxed = true)
        orchestrator.onChooseAddressResult(mockk())
        verify { orchestrator.setOnChooseAddressResult(any()) }
        unmockkStatic("me.proton.core.auth.presentation.AuthOrchestratorKt")
    }
}
