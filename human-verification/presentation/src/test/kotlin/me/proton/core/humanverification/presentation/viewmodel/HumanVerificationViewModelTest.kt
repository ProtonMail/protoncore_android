/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetSettings
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HumanVerificationViewModelTest : CoroutinesTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val humanVerificationWorkflowHandler = mockk<HumanVerificationWorkflowHandler>(relaxed = true)
    private val accountRepository = mockk<AccountRepository>(relaxed = true)
    private val getSettings = mockk<GetSettings>(relaxed = true)
    private val networkPrefs = mockk<NetworkPrefs>(relaxed = true)

    lateinit var viewModel: HumanVerificationViewModel

    private val clientId: ClientId by lazy {
        val id = "client_id"
        val sessionId = SessionId(id)
        ClientId.newClientId(sessionId, null)!!
    }

    @Before
    fun setup() {
        viewModel = HumanVerificationViewModel(
            humanVerificationWorkflowHandler,
            accountRepository,
            getSettings,
            networkPrefs,
        )
    }

    @Test
    fun `onHumanVerificationResult with token calls success`() = coroutinesTest {
        val token = HumanVerificationToken("1234", TokenType.SMS.value)
        viewModel.onHumanVerificationResult(clientId, token)

        coVerify {
            humanVerificationWorkflowHandler.handleHumanVerificationSuccess(
                clientId,
                TokenType.SMS.value,
                "1234"
            )
        }
    }

    @Test
    fun `onHumanVerificationResult with no token calls failure`() = coroutinesTest {
        viewModel.onHumanVerificationResult(clientId, null)

        coVerify { humanVerificationWorkflowHandler.handleHumanVerificationFailed(clientId) }
    }

    @Test
    fun `getHumanVerificationExtraParams with no primary user returns null`() = runBlocking {
        every { accountRepository.getPrimaryUserId() } returns emptyFlow<UserId>()
        val params = viewModel.getHumanVerificationExtraParams()
        assertNull(params)
    }

    @Test
    fun `getHumanVerificationExtraParams with primary user returns extra parameters`() = runBlocking {
        every { accountRepository.getPrimaryUserId() } returns flowOf(UserId("some_user_id"))
        val settingsMock = mockk<UserSettings>().apply {
            every { locale } returns "en_US"
            every { phone } returns RecoverySetting("123456789", 0, false, false)
        }
        coEvery { getSettings.invoke(any()) } returns settingsMock
        val params = viewModel.getHumanVerificationExtraParams()
        assertNotNull(params)
        assertEquals("US", params?.defaultCountry)
        assertEquals("en_US", params?.locale)
        assertEquals("123456789", params?.recoveryPhone)
    }
}
