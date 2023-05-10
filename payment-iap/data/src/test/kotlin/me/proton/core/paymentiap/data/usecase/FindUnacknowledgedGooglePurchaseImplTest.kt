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

package me.proton.core.paymentiap.data.usecase

import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingQuerySubscriptionsTotal
import me.proton.core.observability.domain.metrics.common.GiapStatus
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import me.proton.core.paymentiap.domain.toGiapStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FindUnacknowledgedGooglePurchaseImplTest {
    private lateinit var googleBillingRepository: GoogleBillingRepository
    private lateinit var observabilityManager: ObservabilityManager
    private lateinit var tested: FindUnacknowledgedGooglePurchaseImpl

    @BeforeTest
    fun setUp() {
        googleBillingRepository = mockk(relaxed = true)
        observabilityManager = mockk(relaxed = true)
        tested = FindUnacknowledgedGooglePurchaseImpl({ googleBillingRepository }, observabilityManager)
    }

    @Test
    fun `no purchases`() = runTest {
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns emptyList()
        assertTrue(tested().isEmpty())
    }

    @Test
    fun `acknowledged purchase`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns true
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertTrue(tested().isEmpty())
    }

    @Test
    fun `unacknowledged purchase`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns mockAccountIdentifiers()
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertContentEquals(listOf(purchase.wrap()), tested())
    }

    @Test
    fun `unacknowledged purchase with unmatched product`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns null
            every { products } returns listOf("product-B")
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertNull(tested.byProduct("product-A"))
    }

    @Test
    fun `unacknowledged purchase with matched product`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns null
            every { products } returns listOf("product-A")
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertEquals(purchase.wrap(), tested.byProduct("product-A"))
    }

    @Test
    fun `unacknowledged purchase with unmatched user`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns mockk {
                every { obfuscatedAccountId } returns "customer-B"
            }
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertNull(tested.byCustomer("customer-A"))
    }

    @Test
    fun `unacknowledged purchase with matched user`() = runTest {
        val purchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns mockAccountIdentifiers("customer-A")
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertEquals(purchase.wrap(), tested.byCustomer("customer-A"))
    }

    @Test
    fun `multiple unacknowledged purchases`() = runTest {
        val purchaseA = mockk<Purchase> {
            every { purchaseTime } returns 1200
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns mockAccountIdentifiers("customer-A")
        }
        val purchaseB = mockk<Purchase> {
            every { purchaseTime } returns 1300
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { accountIdentifiers } returns mockAccountIdentifiers("customer-A")
        }
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchaseA, purchaseB)
        assertContentEquals(listOf(purchaseB.wrap(), purchaseA.wrap()), tested())
    }

    @Test
    fun `returns empty list if Billing service is unavailable`() = runTest {
        coEvery { googleBillingRepository.querySubscriptionPurchases() } throws BillingClientError(
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            "Unavailable"
        )
        assertTrue(tested().isEmpty())
    }

    @Test
    fun `rethrows an error`() = runTest {
        coEvery { googleBillingRepository.querySubscriptionPurchases() } throws BillingClientError(
            BillingClient.BillingResponseCode.ERROR,
            "Error"
        )
        assertFailsWith<BillingClientError> { tested() }
    }

    @Test
    fun `observability data is recorder for querying subscriptions`() = runTest {
        // GIVEN
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns emptyList()

        // WHEN
        tested(
            querySubscriptionsMetricData = { result ->
                result.toGiapStatus()?.let { CheckoutGiapBillingQuerySubscriptionsTotal(it) }
            }
        )

        // THEN
        val dataSlot = slot<CheckoutGiapBillingQuerySubscriptionsTotal>()
        verify { observabilityManager.enqueue(capture(dataSlot), any()) }
        assertEquals(GiapStatus.success, dataSlot.captured.Labels.status)
    }

    private fun mockAccountIdentifiers(customerId: String? = null): AccountIdentifiers =
        mockk { every { obfuscatedAccountId } returns customerId }
}
