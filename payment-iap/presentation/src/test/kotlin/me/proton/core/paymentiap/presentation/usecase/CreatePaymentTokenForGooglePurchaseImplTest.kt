/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.paymentiap.presentation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.usecase.CreateOmnichannelPaymentToken
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.presentation.entity.mockPurchase
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.ObserveUserCurrency
import me.proton.core.plan.domain.usecase.ValidateSubscriptionPlan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CreatePaymentTokenForGooglePurchaseImplTest {
    @MockK
    private lateinit var clientIdProvider: ClientIdProvider

    @MockK
    private lateinit var createOmnichannelPaymentToken: CreateOmnichannelPaymentToken

    @MockK
    private lateinit var createPaymentToken: CreatePaymentToken

    @MockK
    private lateinit var isOmnichannelEnabled: IsOmnichannelEnabled

    @MockK
    private lateinit var observeUserCurrency: ObserveUserCurrency

    @MockK
    private lateinit var validateSubscriptionPlan: ValidateSubscriptionPlan

    private lateinit var tested: CreatePaymentTokenForGooglePurchaseImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = CreatePaymentTokenForGooglePurchaseImpl(
            isOmnichannelEnabled = isOmnichannelEnabled,
            createOmnichannelPaymentToken = createOmnichannelPaymentToken,
            createPaymentToken = createPaymentToken
        )
    }

    @Test
    fun `purchase doesn't contain required product ID`() = runTest {
        // GIVEN
        val testProductId = ProductId("google-product-id")
        val purchase = mockPurchase(products = listOf("unknown-product-id")).wrap()

        // WHEN & THEN
        val error = assertFailsWith<IllegalArgumentException> {
            tested(testProductId, purchase, null)
        }
        assertTrue(error.message == "Failed requirement.")
    }

    @Test
    fun `successful result for new user`() = runTest {
        // GIVEN
        val testClientId = mockk<ClientId>()
        val testAmount = 4999L
        val testCurrency = Currency.CHF
        val testCycle = SubscriptionCycle.YEARLY
        val testPaymentToken = ProtonPaymentToken("payment-token")
        val testProductId = ProductId("google-product-id")
        val purchase = mockPurchase(
            products = listOf(testProductId.id),
            purchaseToken = "purchase-token",
            orderId = "order-id",
            packageName = "package-name",
            accountIdentifiers = mockk { every { obfuscatedAccountId } returns "customer-id" }
        ).wrap()

        coEvery { clientIdProvider.getClientId(any()) } returns testClientId
        coEvery { observeUserCurrency(any()) } returns flowOf(testCurrency.name)
        coEvery { validateSubscriptionPlan(any(), any(), any(), any(), any()) } returns mockk {
            every { amountDue } returns testAmount
            every { currency } returns testCurrency
            every { cycle } returns testCycle
        }
        coEvery { isOmnichannelEnabled(any()) } returns false
        coEvery {
            createPaymentToken(any(), any())
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE,
            approvalUrl = null,
            token = testPaymentToken,
            returnHost = null
        )

        // WHEN
        val result = tested(
            googleProductId = testProductId,
            purchase = purchase,
            userId = null
        )

        // THEN
        assertEquals(
            expected = CreatePaymentTokenForGooglePurchase.Result(testPaymentToken),
            actual = result
        )
    }
}
