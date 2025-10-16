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

package me.proton.core.plan.data.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.PerformSubscribe
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PerformSubscribeTest {
    // region mocks
    @MockK(relaxed = true)
    private lateinit var repository: PlansRepository

    @MockK(relaxed = true)
    private lateinit var humanVerificationManager: HumanVerificationManager

    @MockK(relaxed = true)
    private lateinit var clientIdProvider: ClientIdProvider

    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPlanName = "test-plan-name"
    private val testPaymentToken = ProtonPaymentToken("test-payment-token")
    private val testSubscriptionId = "test-subscription-id"
    private val testSubscription = Subscription(
        id = testSubscriptionId,
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1L,
        periodEnd = 2L,
        couponCode = null,
        currency = "EUR",
        amount = 5,
        discount = 0,
        renewDiscount = 0,
        renewAmount = 5,
        plans = listOf(mockk()),
        external = SubscriptionManagement.PROTON_MANAGED,
        customerId = null
    )
    // endregion

    private lateinit var useCase: PerformSubscribe

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        useCase = PerformSubscribeImpl(
            Optional.empty(),
            repository,
            humanVerificationManager,
            clientIdProvider
        )
        coEvery {
            repository.createOrUpdateSubscription(testUserId, any(), any(), any())
        } returns testSubscription
    }

    @Test
    fun `payment token should not be null`() {
        runTest {
            val throwable = assertFailsWith(IllegalArgumentException::class) {
                useCase.invoke(
                    userId = testUserId,
                    cycle = SubscriptionCycle.YEARLY,
                    planNames = listOf(testPlanName),
                    paymentToken = null
                )
            }
            assertNotNull(throwable)
            assertEquals(
                expected = "Required value was null.",
                actual = throwable.message
            )
        }
    }

    @Test
    fun `negative amount is handled correctly`() = runTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                paymentToken = null
            )
        }
    }

    @Test
    fun `negative amount google managed is handled correctly`() = runTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                paymentToken = null
            )
        }
    }

    @Test
    fun `no plans is handled correctly`() = runTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(),
                paymentToken = null
            )
        }
    }

    @Test
    fun `no plans google managed is handled correctly`() = runTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(),
                paymentToken = null
            )
        }
    }

    @Test
    fun `happy path is handled correctly`() = runTest {
        val result = useCase.invoke(
            userId = testUserId,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            paymentToken = testPaymentToken
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                payment = PaymentTokenEntity(testPaymentToken),
                plans = listOf(testPlanName).associateWith { 1 },
                cycle = SubscriptionCycle.YEARLY
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `happy path google managed is handled correctly`() = runTest {
        val result = useCase.invoke(
            userId = testUserId,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            paymentToken = testPaymentToken
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                payment = PaymentTokenEntity(testPaymentToken),
                plans = listOf(testPlanName).associateWith { 1 },
                cycle = SubscriptionCycle.YEARLY
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `repository returns error handled correctly`() = runTest {
        coEvery {
            repository.createOrUpdateSubscription(testUserId, any(), any(), any())
        } throws ApiException(ApiResult.Error.Connection(false, RuntimeException("Test error")))

        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(
                userId = testUserId,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                paymentToken = testPaymentToken
            )
        }
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }

    @Test
    fun `payment token is cleared on successful subscription`() = runTest {
        useCase.invoke(
            userId = testUserId,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            paymentToken = ProtonPaymentToken("token")
        )

        coVerify(atLeast = 1) { humanVerificationManager.clearDetails(any()) }
    }

    @Test
    fun `optional acknowledge purchase present successful subscription`() = runTest {
        val acknowledgeGooglePlayPurchase = mockk<AcknowledgeGooglePlayPurchase>(relaxed = true)
        val acknowledgeGooglePlayPurchaseOptional = mockk<Optional<AcknowledgeGooglePlayPurchase>>(relaxed = true)
        every { acknowledgeGooglePlayPurchaseOptional.isPresent } returns true
        every { acknowledgeGooglePlayPurchaseOptional.get() } returns acknowledgeGooglePlayPurchase
        useCase = PerformSubscribeImpl(
            acknowledgeGooglePlayPurchaseOptional,
            repository,
            humanVerificationManager,
            clientIdProvider
        )
        useCase.invoke(
            userId = testUserId,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            paymentToken = testPaymentToken
        )

        coVerify(exactly = 1) { humanVerificationManager.clearDetails(any()) }
        coVerify(exactly = 1) { acknowledgeGooglePlayPurchase.invoke(testPaymentToken) }
    }
}
