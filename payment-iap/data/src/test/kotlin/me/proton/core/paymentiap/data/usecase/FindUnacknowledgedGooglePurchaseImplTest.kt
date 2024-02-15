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

package me.proton.core.paymentiap.data.usecase

import android.app.Activity
import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FindUnacknowledgedGooglePurchaseImplTest {
    private lateinit var googleBillingRepository: GoogleBillingRepository<Activity>
    private lateinit var observabilityManager: ObservabilityManager
    private lateinit var tested: FindUnacknowledgedGooglePurchaseImpl

    private fun mockAccountIdentifiers(
        customerId: String? = null
    ): AccountIdentifiers = mockk<AccountIdentifiers> {
        every { this@mockk.obfuscatedAccountId } returns customerId
    }

    private fun mockPurchase(
        accountIdentifiers: AccountIdentifiers? = mockAccountIdentifiers(),
        isAcknowledged: Boolean = false,
        purchaseTime: Long = 0,
        purchaseState: Int = PurchaseState.PURCHASED,
        products: List<String> = listOf("product1")
    ): Purchase = mockk<Purchase> {
        every { this@mockk.accountIdentifiers } returns accountIdentifiers
        every { this@mockk.orderId } returns "orderId"
        every { this@mockk.packageName } returns "packageName"
        every { this@mockk.purchaseState } returns purchaseState
        every { this@mockk.purchaseTime } returns purchaseTime
        every { this@mockk.purchaseToken } returns "token"
        every { this@mockk.products } returns products
        every { this@mockk.isAcknowledged } returns isAcknowledged
    }

    @BeforeTest
    fun setUp() {
        googleBillingRepository = mockk(relaxed = true)
        observabilityManager = mockk(relaxed = true)
        tested = FindUnacknowledgedGooglePurchaseImpl { googleBillingRepository }
    }

    @Test
    fun `no purchases`() = runTest {
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns emptyList()
        assertTrue(tested().isEmpty())
    }

    @Test
    fun `acknowledged purchase`() = runTest {
        val purchase = mockPurchase(
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = true
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertTrue(tested().isEmpty())
    }

    @Test
    fun `unacknowledged purchase`() = runTest {
        val purchase = mockPurchase(
            purchaseState= PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = mockAccountIdentifiers()
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertContentEquals(listOf(purchase), tested())
    }

    @Test
    fun `unacknowledged purchase with unmatched product`() = runTest {
        val purchase = mockPurchase(
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = null,
            products = listOf("product-B")
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertNull(tested.byProduct(ProductId("product-A")))
    }

    @Test
    fun `unacknowledged purchase with matched product`() = runTest {
        val purchase = mockPurchase(
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = null,
            products = listOf("product-A")
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertEquals(purchase, tested.byProduct(ProductId("product-A")))
    }

    @Test
    fun `unacknowledged purchase with unmatched user`() = runTest {
        val purchase = mockPurchase(
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = mockk { every { obfuscatedAccountId } returns "customer-B" }
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertNull(tested.byCustomer("customer-A"))
    }

    @Test
    fun `unacknowledged purchase with matched user`() = runTest {
        val purchase = mockPurchase(
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = mockAccountIdentifiers("customer-A")
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(purchase)
        assertEquals(purchase, tested.byCustomer("customer-A"))
    }

    @Test
    fun `multiple unacknowledged purchases`() = runTest {
        val purchaseA = mockPurchase(
            purchaseTime = 1200,
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = mockAccountIdentifiers("customer-A")
        ).wrap()
        val purchaseB = mockPurchase(
            purchaseTime = 1300,
            purchaseState = PurchaseState.PURCHASED,
            isAcknowledged = false,
            accountIdentifiers = mockAccountIdentifiers("customer-A")
        ).wrap()
        coEvery { googleBillingRepository.querySubscriptionPurchases() } returns listOf(
            purchaseA,
            purchaseB
        )
        assertContentEquals(listOf(purchaseB, purchaseA), tested())
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
}
