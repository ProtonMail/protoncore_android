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

package me.proton.core.payment.presentation.viewmodel

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotal
import me.proton.core.observability.domain.metrics.CheckoutCardBillingCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutCardBillingValidatePlanTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingValidatePlanTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsSubscribeTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsValidatePlanTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.extension.getCreatePaymentTokenObservabilityData
import me.proton.core.payment.domain.extension.getSubscribeObservabilityData
import me.proton.core.payment.domain.extension.getValidatePlanObservabilityData
import me.proton.core.payment.domain.usecase.PaymentProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ObservabilityPaymentTypeKtTest {

    @Test
    fun `getCreatePaymentTokenObservabilityData credit card`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getCreatePaymentTokenObservabilityData(PaymentType.CreditCard(mockk()))
        assertNotNull(observabilityResult)
        assertIs<CheckoutCardBillingCreatePaymentTokenTotal>(observabilityResult)
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData payment method`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getCreatePaymentTokenObservabilityData(PaymentType.PaymentMethod("test"))
        assertNotNull(observabilityResult)
        assertIs<CheckoutPaymentMethodsCreatePaymentTokenTotal>(observabilityResult)
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData GoogleIAP`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getCreatePaymentTokenObservabilityData(
            PaymentType.GoogleIAP(
                "test-prod-id",
                GooglePurchaseToken("test-google-token"),
                "test-order-id",
                "test-pkg-name",
                "test-cus-id"
            )
        )
        assertNotNull(observabilityResult)
        assertIs<CheckoutGiapBillingCreatePaymentTokenTotal>(observabilityResult)
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<NotImplementedError> {
            result.getCreatePaymentTokenObservabilityData(PaymentType.PayPal)
        }
    }

    @Test
    fun `getSubscribeObservabilityData credit card`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getSubscribeObservabilityData(
            PaymentType.CreditCard(
                Card.CardWithPaymentDetails(
                    number = "123456789",
                    cvc = "123",
                    expirationMonth = "01",
                    expirationYear = "2021",
                    name = "Test",
                    country = "Test Country",
                    zip = "123"
                )
            )
        )
        assertNotNull(observabilityResult)
        assertEquals(
            CheckoutBillingSubscribeTotal.Manager.proton,
            (observabilityResult as CheckoutBillingSubscribeTotal).Labels.manager
        )
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getSubscribeObservabilityData GoogleIAP`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getSubscribeObservabilityData(
            PaymentType.GoogleIAP(
                "test-prod-id",
                GooglePurchaseToken("test-google-token"),
                "test-order-id",
                "test-pkg-name",
                "test-cus-id"
            )
        )
        assertNotNull(observabilityResult)
        assertEquals(
            CheckoutBillingSubscribeTotal.Manager.google,
            (observabilityResult as CheckoutBillingSubscribeTotal).Labels.manager
        )
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getSubscribeObservabilityData payment method`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getSubscribeObservabilityData(
            PaymentType.PaymentMethod("test")
        )
        assertNotNull(observabilityResult)
        assertIs<CheckoutPaymentMethodsSubscribeTotal>(observabilityResult)
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getSubscribeObservabilityData PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<NotImplementedError> {
            result.getSubscribeObservabilityData(PaymentType.PayPal)
        }
    }

    @Test
    fun `getValidatePlanObservabilityData credit card`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getValidatePlanObservabilityData(
            PaymentType.CreditCard(
                Card.CardWithPaymentDetails(
                    number = "123456789",
                    cvc = "123",
                    expirationMonth = "01",
                    expirationYear = "2021",
                    name = "Test",
                    country = "Test Country",
                    zip = "123"
                )
            )
        )
        assertNotNull(observabilityResult)
        assertIs<CheckoutCardBillingValidatePlanTotal>(observabilityResult)
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getValidatePlanObservabilityData GoogleIAP`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getValidatePlanObservabilityData(
            PaymentType.GoogleIAP(
                "test-prod-id",
                GooglePurchaseToken("test-google-token"),
                "test-order-id",
                "test-pkg-name",
                "test-cus-id"
            )
        )
        assertNotNull(observabilityResult)
        assertIs<CheckoutGiapBillingValidatePlanTotal>(observabilityResult)
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getValidatePlanObservabilityData payment method`() {
        mockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
        val result = mockk<Result<*>>()
        every { result.toHttpApiStatus() } returns HttpApiStatus.http2xx
        val observabilityResult = result.getValidatePlanObservabilityData(
            PaymentType.PaymentMethod("test")
        )
        assertNotNull(observabilityResult)
        assertIs<CheckoutPaymentMethodsValidatePlanTotal>(observabilityResult)
        unmockkStatic("me.proton.core.observability.domain.metrics.common.HttpApiStatusKt")
    }

    @Test
    fun `getValidatePlanObservabilityData PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<NotImplementedError> {
            result.getValidatePlanObservabilityData(PaymentType.PayPal)
        }
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData payment provider credit card`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getCreatePaymentTokenObservabilityData(PaymentProvider.CardPayment)
        assertNotNull(observabilityResult)
        assertIs<CheckoutCardBillingCreatePaymentTokenTotal>(observabilityResult)
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData payment provider GoogleIAP`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getCreatePaymentTokenObservabilityData(PaymentProvider.GoogleInAppPurchase)
        assertNotNull(observabilityResult)
        assertIs<CheckoutGiapBillingCreatePaymentTokenTotal>(observabilityResult)
    }

    @Test
    fun `getCreatePaymentTokenObservabilityData payment provider PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<IllegalStateException> {
            result.getCreatePaymentTokenObservabilityData(PaymentProvider.PayPal)
        }
    }

    @Test
    fun `getSubscribeObservabilityData payment provider credit card`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getSubscribeObservabilityData(PaymentProvider.CardPayment)
        assertNotNull(observabilityResult)
        assertIs<CheckoutBillingSubscribeTotal>(observabilityResult)
    }

    @Test
    fun `getSubscribeObservabilityData payment provider GoogleIAP`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getSubscribeObservabilityData(PaymentProvider.GoogleInAppPurchase)
        assertNotNull(observabilityResult)
        assertIs<CheckoutBillingSubscribeTotal>(observabilityResult)
    }

    @Test
    fun `getSubscribeObservabilityData payment provider PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<IllegalStateException> {
            result.getSubscribeObservabilityData(PaymentProvider.PayPal)
        }
    }

    @Test
    fun `getValidatePlanObservabilityData payment provider credit card`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getValidatePlanObservabilityData(PaymentProvider.CardPayment)
        assertNotNull(observabilityResult)
        assertIs<CheckoutCardBillingValidatePlanTotal>(observabilityResult)
    }

    @Test
    fun `getValidatePlanObservabilityData payment provider GoogleIAP`() {
        val result = mockk<Result<*>>()
        val observabilityResult = result.getValidatePlanObservabilityData(PaymentProvider.GoogleInAppPurchase)
        assertNotNull(observabilityResult)
        assertIs<CheckoutGiapBillingValidatePlanTotal>(observabilityResult)
    }

    @Test
    fun `getValidatePlanObservabilityData payment provider PayPal`() {
        val result = mockk<Result<*>>()
        assertFailsWith<IllegalStateException> {
            result.getValidatePlanObservabilityData(PaymentProvider.PayPal)
        }
    }
}