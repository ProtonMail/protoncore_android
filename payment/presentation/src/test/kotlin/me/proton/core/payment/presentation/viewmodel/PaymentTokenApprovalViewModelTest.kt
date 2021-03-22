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

package me.proton.core.payment.presentation.viewmodel

import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.usecase.GetPaymentTokenStatus
import me.proton.core.payment.presentation.entity.SecureEndpoint
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaymentTokenApprovalViewModelTest : ArchTest, CoroutinesTest {
    // region mocks
    private val getPaymentTokenStatus = mockk<GetPaymentTokenStatus>(relaxed = true)
    private val networkManager = mockk<NetworkManager>()
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testToken = "test-token"
    private val secureEndpoint = SecureEndpoint("test-secure-endpoint")
    // endregion

    private lateinit var viewModel: PaymentTokenApprovalViewModel

    @Before
    fun beforeEveryTest() {
        every { networkManager.observe() } returns flowOf(NetworkStatus.Metered)
        viewModel = PaymentTokenApprovalViewModel(getPaymentTokenStatus, secureEndpoint, networkManager)
    }

    @Test
    fun `network manager returns has connection`() = coroutinesTest {
        val observer = mockk<(Boolean) -> Unit>(relaxed = true)
        val arguments = mutableListOf<Boolean>()
        viewModel.networkConnection.observeDataForever(observer)
        viewModel.watchNetwork()
        verify(exactly = 1) { observer(capture(arguments)) }
        assertTrue(arguments[0])
    }

    @Test
    fun `network manager returns different flow of has and has not connection`() = coroutinesTest {
        every { networkManager.observe() } returns flowOf(NetworkStatus.Disconnected, NetworkStatus.Unmetered)
        val observer = mockk<(Boolean) -> Unit>(relaxed = true)
        val arguments = mutableListOf<Boolean>()
        viewModel.networkConnection.observeDataForever(observer)
        viewModel.watchNetwork()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertFalse(arguments[0])
        assertTrue(arguments[1])
    }

    @Test
    fun `web request redirect other host returns false`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-host"
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 0) { observer(capture(arguments)) }
        assertFalse(result)
    }

    @Test
    fun `web request redirect returns false when not canceled success status check`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-secure-endpoint"
        every { testUri.getQueryParameter("cancel") } returns "0"
        coEvery {
            getPaymentTokenStatus.invoke(
                testUserId,
                testToken
            )
        } returns PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 2) { observer(capture(arguments)) }
        assertFalse(result)
        assertIs<PaymentTokenApprovalViewModel.State.Processing>(arguments[0])
        val approvalStatus = arguments[1]
        assertTrue(approvalStatus is PaymentTokenApprovalViewModel.State.Success)
        assertEquals(PaymentTokenStatus.CHARGEABLE, approvalStatus.paymentTokenStatus)
    }

    @Test
    fun `web request redirect returns false when not canceled status check failed`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-secure-endpoint"
        every { testUri.getQueryParameter("cancel") } returns "0"
        coEvery {
            getPaymentTokenStatus.invoke(
                testUserId,
                testToken
            )
        } returns PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.FAILED)
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 2) { observer(capture(arguments)) }
        assertFalse(result)
        assertIs<PaymentTokenApprovalViewModel.State.Processing>(arguments[0])
        val approvalStatus = arguments[1]
        assertTrue(approvalStatus is PaymentTokenApprovalViewModel.State.Success)
        assertEquals(PaymentTokenStatus.FAILED, approvalStatus.paymentTokenStatus)
    }

    @Test
    fun `web request redirect returns false when not canceled status throws api exception`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-secure-endpoint"
        every { testUri.getQueryParameter("cancel") } returns "0"
        coEvery { getPaymentTokenStatus.invoke(testUserId, testToken) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 2) { observer(capture(arguments)) }
        assertFalse(result)
        assertIs<PaymentTokenApprovalViewModel.State.Processing>(arguments[0])
        val approvalStatus = arguments[1]
        assertTrue(approvalStatus is PaymentTokenApprovalViewModel.State.Error.Message)
        assertEquals("proton error", approvalStatus.message)
    }

    @Test
    fun `web request redirect returns false when URI same as return host success status check`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-host"
        every { testUri.getQueryParameter("cancel") } returns "0"
        coEvery { getPaymentTokenStatus.invoke(testUserId, testToken) } returns PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
        val testReturnHost = "test-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 2) { observer(capture(arguments)) }
        assertFalse(result)
        assertIs<PaymentTokenApprovalViewModel.State.Processing>(arguments[0])
        val approvalStatus = arguments[1]
        assertTrue(approvalStatus is PaymentTokenApprovalViewModel.State.Success)
        assertEquals(PaymentTokenStatus.CHARGEABLE, approvalStatus.paymentTokenStatus)
    }

    @Test
    fun `web request redirect returns true when URI same as return host cancelled success status check`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-host"
        every { testUri.getQueryParameter("cancel") } returns "1"
        coEvery { getPaymentTokenStatus.invoke(testUserId, testToken) } returns PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
        val testReturnHost = "test-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 0) { observer(capture(arguments)) }
        assertTrue(result)
    }

    @Test
    fun `web request redirect returns true when URI same as return secure endpoint success status check`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-secure-endpoint"
        every { testUri.getQueryParameter("cancel") } returns "1"
        coEvery { getPaymentTokenStatus.invoke(testUserId, testToken) } returns PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 0) { observer(capture(arguments)) }
        assertTrue(result)
    }

    @Test
    fun `web request redirect other host returns true when canceled`() = coroutinesTest {
        // GIVEN
        val testUri = mockk<Uri>(relaxed = true)
        every { testUri.host } returns "test-host"
        every { testUri.getQueryParameter("cancel") } returns "0"
        val testReturnHost = "test-return-host"
        val observer = mockk<(PaymentTokenApprovalViewModel.State) -> Unit>(relaxed = true)
        val arguments = mutableListOf<PaymentTokenApprovalViewModel.State>()
        viewModel.approvalResult.observeDataForever(observer)
        // WHEN
        val result = viewModel.handleRedirection(testUserId, testToken, testUri, testReturnHost)
        // THEN
        verify(exactly = 0) { observer(capture(arguments)) }
        assertFalse(result)
    }
}
