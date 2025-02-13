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

package me.proton.core.plan.data.api

import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.plan.data.api.request.CreateSubscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.test.kotlin.BuildRetrofitApi
import me.proton.core.test.kotlin.enqueueFromResourceFile
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlansApiTest {
    private lateinit var tested: PlansApi
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
    fun `get current dynamic subscription with customer ID`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("GET/payments/v4/dynamic-subscription.json", javaClass.classLoader)

        // When
        val subscription = tested.getDynamicSubscriptions().subscriptions.first().toDynamicSubscription("endpoint")

        // Then
        assertEquals(28788, subscription.amount)
        assertEquals(SubscriptionManagement.PROTON_MANAGED, subscription.external)
    }

    @Test
    fun `create subscription`() = runTest {
        // Given
        webServer.enqueueFromResourceFile("POST/payments/v4/subscription.json", javaClass.classLoader)

        // When
        val subscription = tested.createUpdateSubscription(
            TimeoutOverride(),
            CreateSubscription(
                amount = 4788,
                currency = "CHF",
                paymentToken = "token-123",
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
