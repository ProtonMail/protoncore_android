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

package me.proton.core.payment.data.api

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import me.proton.core.payment.data.api.request.CreatePaymentToken
import me.proton.core.payment.data.api.request.CreateSubscription
import me.proton.core.payment.data.api.request.IAPDetailsBody
import me.proton.core.payment.data.api.request.PaymentTypeEntity
import me.proton.core.payment.data.api.request.TokenDetails
import me.proton.core.payment.data.api.request.TokenTypePaymentBody
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.test.kotlin.BuildRetrofitApi
import me.proton.core.test.kotlin.enqueueFromResourceFile
import okhttp3.mockwebserver.MockWebServer
import java.nio.charset.Charset
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentsApiTest {
    private lateinit var tested: PaymentsApi
    private lateinit var webServer: MockWebServer

    @BeforeTest
    fun setUp() {
        webServer = MockWebServer()
        tested = BuildRetrofitApi(webServer.url("/"))
    }

    @AfterTest
    fun tearDown() {
        webServer.shutdown()
    }

    @Test
    fun `create payment token from Google purchase token`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("GET/payments/v4/tokens.json", javaClass.classLoader)

        // When
        val result = tested.createPaymentToken(
            CreatePaymentToken(
                amount = 499,
                currency = "EUR",
                paymentEntity = PaymentTypeEntity.GoogleIAP(
                    IAPDetailsBody(
                        productId = "goog_product_1",
                        purchaseToken = "purchase_token",
                        orderId = "order-1",
                        packageName = "package.name",
                        customerId = "customer-1"
                    )
                ),
                paymentMethodId = null
            )
        ).toCreatePaymentTokenResult()

        val recordedRequest = webServer.takeRequest()
        val requestBody = recordedRequest.body.use { buffer ->
            buffer.readString(Charset.defaultCharset())
        }

        // Then
        val expectedRequestJson = Json.parseToJsonElement(
            """
            {
                "Amount": 499,
                "Currency": "EUR",
                "Payment": {
                    "type":"me.proton.core.payment.data.api.request.PaymentTypeEntity.GoogleIAP",
                    "Type": "google",
                    "Details": {
                        "productID": "goog_product_1",
                        "purchaseToken": "purchase_token",
                        "orderID": "order-1",
                        "packageName": "package.name",
                        "customerID": "customer-1"
                    }
                },
                "PaymentMethodID": null
            }
            """.trimIndent()
        )
        val requestJson = Json.parseToJsonElement(requestBody)
        assertEquals(expectedRequestJson, requestJson)

        assertEquals("payment_token", result.token.value)
    }

    @Test
    fun `get current subscription with customer ID`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("GET/payments/v4/subscription.json", javaClass.classLoader)

        // When
        val subscription = tested.getCurrentSubscription().subscription.toSubscription()

        // Then
        assertEquals("customer-1", subscription.customerId)
        assertEquals(SubscriptionManagement.GOOGLE_MANAGED, subscription.external)
        assertEquals(1, subscription.plans.size)
    }

    @Test
    fun `create subscription`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("POST/payments/v4/subscription.json", javaClass.classLoader)

        // When
        val subscription = tested.createUpdateSubscription(
            CreateSubscription(
                amount = 4788,
                currency = "CHF",
                payment = TokenTypePaymentBody(
                    type = "token",
                    tokenDetails = TokenDetails(token = "token-123")
                ),
                codes = null,
                plans = mapOf("mail2022" to 1),
                cycle = 12,
                external = SubscriptionManagement.GOOGLE_MANAGED.value
            )
        ).subscription.toSubscription()

        // Then
        assertNull(subscription.customerId)
        assertEquals(12, subscription.cycle)
        assertEquals(1, subscription.plans.size)
        assertEquals("mail2022", subscription.plans.first().name)
    }
}
