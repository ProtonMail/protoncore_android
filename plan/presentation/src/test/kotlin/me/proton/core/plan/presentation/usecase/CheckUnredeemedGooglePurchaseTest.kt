/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.usecase.GetDynamicPlans
import me.proton.core.plan.domain.usecase.GetDynamicSubscription
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchaseStatus
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import java.time.Instant
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckUnredeemedGooglePurchaseTest {
    private lateinit var userManager: UserManager
    private lateinit var findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase
    private lateinit var findUnacknowledgedGooglePurchaseOptional: Optional<FindUnacknowledgedGooglePurchase>
    private lateinit var getAvailablePaymentProviders: GetAvailablePaymentProviders
    private lateinit var getCurrentSubscription: GetDynamicSubscription
    private lateinit var getPlans: GetDynamicPlans
    private lateinit var tested: CheckUnredeemedGooglePurchase

    @BeforeTest
    fun setUp() {
        userManager = mockk {
            coEvery { getUser(any()) } returns mockk { every { type } returns Type.Proton }
        }
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
            userManager,
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
            every { productIds } returns listOf(ProductId(productId))
        }
        val plan = mockk<DynamicPlan> {
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productId,
                            customerId = "customer-id"
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchase)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns DynamicSubscription(
            name = null,
            title = "",
            description = ""
        )
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

        assertEquals(
            UnredeemedGooglePurchase(googlePurchase, plan, UnredeemedGooglePurchaseStatus.NotSubscribed),
            tested(userId)
        )
    }

    @Test
    fun `plan purchased and user not subscribed, but no corresponding plan`() = runTest {
        val userId = UserId("user-1")
        val googlePurchase = mockk<GooglePurchase> {
            every { productIds } returns listOf(ProductId("google_plan_name"))
        }
        val plan = mockk<DynamicPlan> {
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = "custom_plan_name",
                            customerId = "customer-id"
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchase)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns DynamicSubscription(
            name = null,
            title = "",
            description = ""
        )
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed but plan not managed by google`() = runTest {
        val customerA = "customer-A"
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { productIds } returns listOf(ProductId(productId))
        }
        val plan = mockk<DynamicPlan> {
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productId,
                            customerId = customerA
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk<DynamicSubscription> {
            every { customerId } returns customerA
            every { external } returns SubscriptionManagement.PROTON_MANAGED
        }
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

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
            every { productIds } returns listOf(ProductId(productId))
        }
        val plan = mockk<DynamicPlan> {
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productId,
                            customerId = customerA
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(any()) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk<DynamicSubscription> {
            every { customerId } returns customerB
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
        }
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

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
            every { productIds } returns listOf(ProductId(productId))
        }
        val plan = mockk<DynamicPlan> {
            every { name } returns planA
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productId,
                            customerId = customerA
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk<DynamicSubscription> {
            every { customerId } returns customerA
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
            every { name } returns planB
        }
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

        assertNull(tested(userId))
    }

    @Test
    fun `user subscribed with matching plan`() = runTest {
        val customerA = "customer-A"
        val userId = UserId("user-1")
        val productId = "google_plan_name"
        val googlePurchase = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(ProductId(productId))
        }
        val plan = mockk<DynamicPlan> {
            every { name } returns "plan-A"
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productId,
                            customerId = customerA
                        )
                    )
                )
            )
        }

        coEvery { findUnacknowledgedGooglePurchase.byCustomer(customerA) } returns googlePurchase
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns mockk<DynamicSubscription> {
            every { customerId } returns "customer-A"
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
            every { name } returns "plan-A"
            every { cycleMonths } returns 12
        }
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

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

        val planA = mockk<DynamicPlan> {
            every { name } returns "plan-A"
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productA,
                            customerId = customerA
                        )
                    )
                )
            )
        }
        val planB = mockk<DynamicPlan> {
            every { name } returns "plan-B"
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = productB,
                            customerId = customerB
                        )
                    )
                )
            )
        }

        val googlePurchaseA = mockk<GooglePurchase> {
            every { customerId } returns customerA
            every { productIds } returns listOf(ProductId(productA))
        }
        val googlePurchaseB = mockk<GooglePurchase> {
            every { customerId } returns customerB
            every { productIds } returns listOf(ProductId(productB))
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchaseA, googlePurchaseB)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns DynamicSubscription(
            name = null,
            title = "",
            description = ""
        )
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(planA, planB))

        assertEquals(
            UnredeemedGooglePurchase(googlePurchaseA, planA, UnredeemedGooglePurchaseStatus.NotSubscribed),
            tested(userId)
        )
    }

    @Test
    fun `user not subscribed with 1 unacknowledged google purchases but credential-less`() = runTest {
        val customer = "customer"
        val product = "product"
        val userId = UserId("user-id")

        coEvery { userManager.getUser(userId) } returns mockk {
            every { type } returns Type.CredentialLess
        }

        val plan = mockk<DynamicPlan> {
            every { name } returns "plan"
            every { instances } returns mapOf(
                12 to DynamicPlanInstance(
                    cycle = 12,
                    description = "",
                    periodEnd = Instant.MAX,
                    price = mapOf(),
                    vendors = mapOf(
                        AppStore.GooglePlay to DynamicPlanVendor(
                            productId = product,
                            customerId = customer
                        )
                    )
                )
            )
        }
        val googlePurchase = mockk<GooglePurchase> {
            every { customerId } returns customer
            every { productIds } returns listOf(ProductId(product))
        }

        coEvery { findUnacknowledgedGooglePurchase.invoke() } returns listOf(googlePurchase)
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(userId) } returns DynamicSubscription(
            name = null,
            title = "",
            description = ""
        )
        coEvery { getPlans.invoke(null) } returns DynamicPlans(defaultCycle = null, listOf(plan))

        assertNull(tested(userId))
    }

    @Test
    fun `returns null if null subscription`() = runTest {
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.entries.toSet()
        coEvery { getCurrentSubscription.invoke(any()) } returns null
        assertNull(tested(mockk()))
    }
}
