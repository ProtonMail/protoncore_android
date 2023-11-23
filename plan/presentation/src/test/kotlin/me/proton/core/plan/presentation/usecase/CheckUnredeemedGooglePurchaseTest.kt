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

package me.proton.core.plan.presentation.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanDuration
import me.proton.core.plan.domain.entity.PlanVendorData
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.usecase.GetCurrentSubscription
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchaseStatus
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckUnredeemedGooglePurchaseTest {
    private lateinit var findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase
    private lateinit var findUnacknowledgedGooglePurchaseOptional: Optional<FindUnacknowledgedGooglePurchase>
    private lateinit var getAvailablePaymentProviders: GetAvailablePaymentProviders
    private lateinit var getCurrentSubscription: GetCurrentSubscription
    private lateinit var getPlans: GetPlans
    private lateinit var tested: CheckUnredeemedGooglePurchase

    @BeforeTest
    fun setUp() {
        findUnacknowledgedGooglePurchase = mockk()
        findUnacknowledgedGooglePurchaseOptional = mockk {
            every { isPresent } returns true
            every { get() } returns findUnacknowledgedGooglePurchase
            every { getOrNull() } returns findUnacknowledgedGooglePurchase
        }
        getAvailablePaymentProviders = mockk()
        getCurrentSubscription = mockk()
        getPlans = mockk()

        tested = CheckUnredeemedGooglePurchase(
            findUnacknowledgedGooglePurchaseOptional,
            getAvailablePaymentProviders,
            getCurrentSubscription,
            getPlans
        )
    }

    @Test
    fun `no unacknowledged purchases if FindUnacknowledgedGooglePurchase is not provided`() = runTest {
        every { findUnacknowledgedGooglePurchaseOptional.get() } throws NoSuchElementException()
        every { findUnacknowledgedGooglePurchaseOptional.isPresent } returns false
        assertNull(tested(mockk()))
    }

    @Test
    fun `google payments not available`() = runTest {
        coEvery { getAvailablePaymentProviders.invoke() } returns emptySet()
        assertNull(tested(mockk()))
    }

    @Test
    fun `plan purchased and user not subscribed`() = runTest {
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { productIds } returns listOf(productId)
        }
        val plan = mockk<Plan> {
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    "customer-id",
                    mapOf(PlanDuration(12) to productId)
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchase)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns null
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertEquals(
            UnredeemedGooglePurchase(googlePurchase, plan, UnredeemedGooglePurchaseStatus.NotSubscribed),
            tested(userId)
        )
    }

    @Test
    fun `plan purchased and user not subscribed, but no corresponding plan`() = runTest {
        val userId = UserId("user-1")
        val googlePurchase = mockk<GooglePurchase> {
            every { productIds } returns listOf("google_plan_name")
        }
        val plan = mockk<Plan> {
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    "customer-id",
                    mapOf(PlanDuration(12) to "custom_plan_name")
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchase)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns null
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed but plan not managed by google`() = runTest {
        val customerA = "customer-A"
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { productIds } returns listOf(productId)
        }
        val plan = mockk<Plan> {
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerA,
                    mapOf(PlanDuration(12) to productId)
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk {
            every { customerId } returns customerA
            every { external } returns SubscriptionManagement.PROTON_MANAGED
        }
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed but has different customer ID`() = runTest {
        val customerA = "customer-A"
        val customerB = "customer-B"

        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(productId)
        }
        val plan = mockk<Plan> {
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerA,
                    mapOf(PlanDuration(12) to productId)
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(any()) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk {
            every { customerId } returns customerB
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
        }
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed but has different plan name`() = runTest {
        val planA = "plan-A"
        val planB = "plan-B"

        val customerA = "customer-A"
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(productId)
        }
        val plan = mockk<Plan> {
            every { name } returns planA
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerA,
                    mapOf(PlanDuration(12) to productId)
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk {
            every { customerId } returns customerA
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
            every { plans } returns listOf(mockk { every { name } returns planB })
        }
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed with matching plan`() = runTest {
        val customerA = "customer-A"
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(productId)
        }
        val plan = mockk<Plan> {
            every { name } returns "plan-A"
            every { cycle } returns 12
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerA,
                    mapOf(PlanDuration(12) to productId)
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk {
            every { customerId } returns "customer-A"
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
            every { cycle } returns 12
            every { plans } returns listOf(mockk { every { name } returns "plan-A" })
        }
        coEvery { getPlans.invoke(userId) } returns listOf(plan)

        assertEquals(
            UnredeemedGooglePurchase(googlePurchase, plan, UnredeemedGooglePurchaseStatus.SubscribedButNotAcknowledged),
            tested(userId)
        )
    }

    @Test
    fun `user not subscribed with multiple unacknowledged google purchases`() = runTest {
        val customerA = "customer-A"
        val customerB = "customer-B"
        val productA = "product-A"
        val productB = "product-B"
        val userId = UserId("user-id")

        val planA = mockk<Plan> {
            every { name } returns "plan-A"
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerA,
                    mapOf(PlanDuration(12) to productA)
                )
            )
        }
        val planB = mockk<Plan> {
            every { name } returns "plan-B"
            every { vendors } returns mapOf(
                AppStore.GooglePlay to PlanVendorData(
                    customerB,
                    mapOf(PlanDuration(12) to productB)
                )
            )
        }

        val googlePurchaseA = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(productA)
        }
        val googlePurchaseB = mockk<GooglePurchase> {
            every { customerId } returns customerB
            every { productIds } returns listOf(productB)
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchaseA, googlePurchaseB)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns null
        coEvery { getPlans.invoke(userId) } returns listOf(planA, planB)

        assertEquals(
            UnredeemedGooglePurchase(googlePurchaseA, planA, UnredeemedGooglePurchaseStatus.NotSubscribed),
            tested(userId)
        )
    }

    @Test
    fun `returns null on network error`() = runTest {
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        coEvery { getCurrentSubscription.invoke(any()) } throws ApiException(ApiResult.Error.Connection())
        assertNull(tested(mockk()))
    }
}
