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

package me.proton.core.humanverification.presentation.viewmodel.hv3

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.HvPageLoadTotal
import me.proton.core.observability.domain.metrics.HvPageLoadTotal.Routing
import me.proton.core.observability.domain.metrics.HvPageLoadTotal.Status
import me.proton.core.observability.domain.metrics.HvScreenViewTotal
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HV3ViewModelTest : CoroutinesTest by UnconfinedCoroutinesTest() {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    private val humanVerificationWorkflowHandler = mockk<HumanVerificationWorkflowHandler>(relaxed = true)
    private val accountRepository = mockk<AccountRepository>(relaxed = true)
    private val getSettings = mockk<GetUserSettings>(relaxed = true)
    private val networkPrefs = mockk<NetworkPrefs>(relaxed = true)
    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val telemetryManager = mockk<TelemetryManager>(relaxed = true)

    lateinit var viewModel: HV3ViewModel

    private val clientId: ClientId by lazy {
        val id = "client_id"
        val sessionId = SessionId(id)
        ClientId.newClientId(sessionId, null)!!
    }

    @Before
    fun setup() {
        viewModel = HV3ViewModel(
            humanVerificationWorkflowHandler,
            observabilityManager,
            accountRepository,
            getSettings,
            networkPrefs,
            Product.Mail,
            telemetryManager
        )
    }

    @Test
    fun `onHumanVerificationResult with token calls success`() = coroutinesTest {
        val token = HumanVerificationToken("1234", TokenType.SMS.value)
        viewModel.onHumanVerificationResult(clientId, token, false)

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
        viewModel.onHumanVerificationResult(clientId, null, false)

        coVerify { humanVerificationWorkflowHandler.handleHumanVerificationFailed(clientId) }
    }

    @Test
    fun `getHumanVerificationExtraParams with no primary user returns empty extra params`() = coroutinesTest {
        every { accountRepository.getPrimaryUserId() } returns emptyFlow<UserId>()
        val params = viewModel.getHumanVerificationExtraParams()
        assertNull(params.defaultCountry)
        assertNull(params.locale)
        assertNull(params.recoveryPhone)
    }

    @Test
    fun `getHumanVerificationExtraParams with primary user returns extra parameters`() = coroutinesTest {
        every { accountRepository.getPrimaryUserId() } returns flowOf(UserId("some_user_id"))
        val settingsMock = mockk<UserSettings>().apply {
            every { locale } returns "en_US"
            every { phone } returns RecoverySetting("123456789", 0, false, false)
        }
        coEvery { getSettings.invoke(any(), any()) } returns settingsMock
        val params = viewModel.getHumanVerificationExtraParams()
        assertNotNull(params)
        assertEquals("US", params?.defaultCountry)
        assertEquals("en_US", params?.locale)
        assertEquals("123456789", params?.recoveryPhone)
    }

    @Test
    fun `getHumanVerificationExtraParams returns useVPNTheme when product is Vpn`() = coroutinesTest {
        viewModel = HV3ViewModel(
            humanVerificationWorkflowHandler,
            observabilityManager,
            accountRepository,
            getSettings,
            networkPrefs,
            Product.Vpn,
            telemetryManager
        )
        every { accountRepository.getPrimaryUserId() } returns flowOf(UserId("some_user_id"))
        val settingsMock = mockk<UserSettings>().apply {
            every { locale } returns "en_US"
            every { phone } returns RecoverySetting("123456789", 0, false, false)
        }
        coEvery { getSettings.invoke(any(), any()) } returns settingsMock
        val params = viewModel.getHumanVerificationExtraParams()
        assertEquals(true, params?.useVPNTheme)
    }

    @Test
    fun `when onScreenView, enqueue hv observability screenView`() = coroutinesTest {
        // GIVEN
        val screenView = HvScreenViewTotal(HvScreenViewTotal.ScreenId.hv3)

        viewModel = HV3ViewModel(
            humanVerificationWorkflowHandler,
            observabilityManager,
            accountRepository,
            getSettings,
            networkPrefs,
            Product.Vpn,
            telemetryManager
        )
        // WHEN
        viewModel.onScreenView()
        // THEN
        verify { observabilityManager.enqueue(screenView, any()) }
    }

    @Test
    fun `when onPageLoad, enqueue hv observability pageLoad, standard routing`() = coroutinesTest {
        // GIVEN
        val pageLoad = HvPageLoadTotal(status = Status.http2xx, routing = Routing.standard)
        every { networkPrefs.activeAltBaseUrl } returns null

        viewModel = HV3ViewModel(
            humanVerificationWorkflowHandler,
            observabilityManager,
            accountRepository,
            getSettings,
            networkPrefs,
            Product.Vpn,
            telemetryManager
        )
        // WHEN
        viewModel.onPageLoad(pageLoad.Labels.status)
        // THEN
        verify { observabilityManager.enqueue(pageLoad, any()) }
    }

    @Test
    fun `when onPageLoad, enqueue hv observability pageLoad, alternative routing`() = coroutinesTest {
        // GIVEN
        val pageLoad = HvPageLoadTotal(status = Status.http2xx, routing = Routing.alternative)
        every { networkPrefs.activeAltBaseUrl } returns "alternativeBaseUrl"

        viewModel = HV3ViewModel(
            humanVerificationWorkflowHandler,
            observabilityManager,
            accountRepository,
            getSettings,
            networkPrefs,
            Product.Vpn,
            telemetryManager
        )
        // WHEN
        viewModel.onPageLoad(pageLoad.Labels.status)
        // THEN
        verify { observabilityManager.enqueue(pageLoad, any()) }
    }
}
