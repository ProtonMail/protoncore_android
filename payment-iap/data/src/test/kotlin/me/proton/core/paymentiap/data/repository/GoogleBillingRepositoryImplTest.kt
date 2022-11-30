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

package me.proton.core.paymentiap.data.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.PurchasesUpdatedListener
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.test.kotlin.TestDispatcherProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class GoogleBillingRepositoryImplTest {
    private lateinit var billingClientFactory: FakeBillingClientFactory
    private lateinit var factory: FakeConnectedBillingClientFactory
    private lateinit var tested: GoogleBillingRepositoryImpl

    @BeforeTest
    fun setUp() {
        billingClientFactory = FakeBillingClientFactory()
        factory = FakeConnectedBillingClientFactory()
        tested = GoogleBillingRepositoryImpl(factory, TestDispatcherProvider())
    }

    @Test
    fun `repository is destroyed after use`() = runTest {
        tested.use {}
        verify { factory.connectedBillingClient.destroy() }
        verify { tested.destroy() }
    }

    @Test
    fun `acknowledge a purchase`() = runTest {
        coEvery { factory.connectedBillingClient.withClient<BillingResult>(any()) } returns BillingResult()
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }
    }

    @Test(expected = BillingClientError::class)
    fun `fails to acknowledge a purchase`() = runTest {
        coEvery { factory.connectedBillingClient.withClient<BillingResult>(any()) } returns
            BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.ERROR).build()
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }
    }

    @Test
    fun `get product details`() = runTest {
        val productDetails = mockk<ProductDetails>()
        val productDetailsResult = ProductDetailsResult(BillingResult(), listOf(productDetails))
        coEvery { factory.connectedBillingClient.withClient<ProductDetailsResult>(any()) } returns productDetailsResult
        val result = tested.use { it.getProductDetails("plan-name") }
        assertSame(result, productDetails)
    }

    @Test
    fun `launch billing flow`() = runTest {
        coEvery { factory.connectedBillingClient.withClient<BillingResult>(any()) } returns BillingResult()
        tested.use { it.launchBillingFlow(mockk(), mockk()) }
    }

    @Test
    fun `query subscription purchases`() = runTest {
        val purchaseList = listOf(mockk<Purchase>())
        val purchaseResult = PurchasesResult(BillingResult(), purchaseList)
        coEvery { factory.connectedBillingClient.withClient<PurchasesResult>(any()) } returns purchaseResult
        val result = tested.use { it.querySubscriptionPurchases() }
        assertSame(result, purchaseList)
    }

    @Test
    fun `fails to connect`() {
        coEvery { factory.connectedBillingClient.withClient<BillingResult>(any()) } throws BillingClientError(
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, "test error"
        )
        runTest {
            assertFailsWith<BillingClientError> {
                tested.use { it.launchBillingFlow(mockk(), mockk()) }
            }
        }
    }
}

private class FakeBillingClientFactory : BillingClientFactory {
    lateinit var listener: PurchasesUpdatedListener

    override fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): BillingClient {
        listener = purchasesUpdatedListener
        return mockk(relaxed = true)
    }
}

private class FakeConnectedBillingClientFactory : ConnectedBillingClientFactory {
    val connectedBillingClient: ConnectedBillingClient = mockk(relaxed = true)

    override fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): ConnectedBillingClient =
        connectedBillingClient
}
