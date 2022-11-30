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

package me.proton.core.payment.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.AppStore
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.PaymentStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAvailablePaymentProvidersTest {
    private lateinit var accountManager: AccountManager
    private lateinit var getPaymentStatus: GetPaymentStatus
    private lateinit var protonIAPBillingLibrary: ProtonIAPBillingLibrary
    private lateinit var tested: GetAvailablePaymentProviders

    @BeforeTest
    fun setUp() {
        accountManager = mockk { every { getPrimaryUserId() } returns flowOf(null) }
        getPaymentStatus = mockk()
        protonIAPBillingLibrary = mockk()
    }

    @Test
    fun `payments status unknown`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockPaymentStatus(PaymentStatus(null, null, null))

        assertEquals(
            emptySet(),
            tested()
        )
    }

    @Test
    fun `all payment status disabled`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockGoogleIAP(true)
        mockPaymentStatus(PaymentStatus(card = false, inApp = false, paypal = false))

        assertEquals(
            emptySet(),
            tested()
        )
    }

    @Test
    fun `payments enabled and all providers available`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockGoogleIAP(true)
        mockPaymentStatus(PaymentStatus(card = true, inApp = true, paypal = true))

        assertEquals(
            setOf(PaymentProvider.GoogleInAppPurchase, PaymentProvider.CardPayment, PaymentProvider.PayPal),
            tested()
        )
    }

    @Test
    fun `all payments enabled but missing Google IAP library`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockGoogleIAP(false)
        mockPaymentStatus(PaymentStatus(card = true, inApp = true, paypal = true))

        assertEquals(
            setOf(PaymentProvider.CardPayment, PaymentProvider.PayPal),
            tested()
        )
    }

    @Test
    fun `only Proton Card payments enabled`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockGoogleIAP(true)
        mockPaymentStatus(PaymentStatus(card = true, inApp = false, paypal = false))

        assertEquals(
            setOf(PaymentProvider.CardPayment),
            tested()
        )
    }

    @Test
    fun `only Google IAP enabled`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        mockGoogleIAP(true)
        mockPaymentStatus(PaymentStatus(card = false, inApp = true, paypal = false))

        assertEquals(
            setOf(PaymentProvider.GoogleInAppPurchase),
            tested()
        )
    }

    @Test
    fun `Google IAP is not available on F-Droid builds`() = runTest {
        tested = makeTested(AppStore.FDroid)
        mockPaymentStatus(PaymentStatus(card = true, inApp = true, paypal = true))

        assertEquals(
            setOf(PaymentProvider.CardPayment, PaymentProvider.PayPal),
            tested()
        )
        verify(exactly = 0) { protonIAPBillingLibrary.isAvailable() }
    }

    @Test
    fun `does not throw an API exception`() = runTest {
        tested = makeTested(AppStore.GooglePlay)
        coEvery { getPaymentStatus.invoke(any(), any()) } throws ApiException(ApiResult.Error.Http(500, "Server error"))

        assertTrue(tested().isEmpty())
    }

    private fun mockGoogleIAP(available: Boolean) {
        every { protonIAPBillingLibrary.isAvailable() } returns available
    }

    private fun mockPaymentStatus(paymentStatus: PaymentStatus) {
        coEvery { getPaymentStatus.invoke(any(), any()) } returns paymentStatus
    }

    private fun makeTested(appStore: AppStore): GetAvailablePaymentProviders =
        GetAvailablePaymentProviders(
            accountManager,
            appStore,
            getPaymentStatus,
            protonIAPBillingLibrary
        )
}
