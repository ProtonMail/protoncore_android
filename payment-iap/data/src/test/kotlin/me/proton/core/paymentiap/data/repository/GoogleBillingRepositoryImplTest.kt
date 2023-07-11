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

import app.cash.turbine.test
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.runTestWithResultContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GoogleBillingRepositoryImplTest {
    private lateinit var billingClientFactory: FakeBillingClientFactory
    private lateinit var factory: FakeConnectedBillingClientFactory
    private lateinit var testDispatcherProvider: TestDispatcherProvider
    private lateinit var tested: GoogleBillingRepositoryImpl

    @BeforeTest
    fun setUp() {
        billingClientFactory = FakeBillingClientFactory()
        factory = FakeConnectedBillingClientFactory()
        testDispatcherProvider = TestDispatcherProvider()
        tested = GoogleBillingRepositoryImpl(factory, testDispatcherProvider)
    }

    @Test
    fun `repository is destroyed after use`() = runTest {
        tested.use {}
        verify { factory.connectedBillingClient.destroy() }
        verify { tested.destroy() }
    }

    @Test
    fun `acknowledge a purchase`() = runTest {
        mockClientResult {
            every { acknowledgePurchase(any(), any()) } answers {
                val listener = invocation.args[1] as AcknowledgePurchaseResponseListener
                listener.onAcknowledgePurchaseResponse(BillingResult())
            }
        }
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }
    }

    @Test(expected = BillingClientError::class)
    fun `fails to acknowledge a purchase`() = runTest {
        mockClientResult {
            every { acknowledgePurchase(any(), any()) } answers {
                val listener = invocation.args[1] as AcknowledgePurchaseResponseListener
                val result = BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                    .build()
                listener.onAcknowledgePurchaseResponse(result)
            }
        }
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }
    }

    @Test
    fun `get product details`() = runTest {
        val productDetails = mockk<ProductDetails>()
        mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                listener.onProductDetailsResponse(BillingResult(), listOf(productDetails))
            }
        }
        val result = tested.use { it.getProductDetails("plan-name") }
        assertSame(result, productDetails)
    }

    @Test
    fun `get product details null`() = runTestWithResultContext {
        mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                listener.onProductDetailsResponse(BillingResult(), listOf())
            }
        }
        val result = tested.use { it.getProductDetails("plan-name") }
        assertSame(result, null)
        assertTrue(assertSingleResult("getProductDetails").isSuccess)
    }

    @Test
    fun `launch billing flow`() = runTestWithResultContext {
        mockClientResult {
            every { launchBillingFlow(any(), any()) } returns BillingResult()
        }
        tested.use { it.launchBillingFlow(mockk(), mockk()) }
        assertTrue(assertSingleResult("launchBillingFlow").isSuccess)
    }

    @Test
    fun `query subscription purchases`() = runTestWithResultContext {
        val purchaseList = listOf(mockk<Purchase>())
        val purchaseResult = PurchasesResult(BillingResult(), purchaseList)
        mockClientResult {
            every { queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
                val listener = invocation.args[1] as PurchasesResponseListener
                listener.onQueryPurchasesResponse(
                    purchaseResult.billingResult,
                    purchaseResult.purchasesList
                )
            }
        }
        val result = tested.use { it.querySubscriptionPurchases() }
        assertSame(result, purchaseList)
        assertTrue(assertSingleResult("querySubscriptionPurchases").isSuccess)
    }

    @Test
    fun `query subscription purchases yielding`() = runTestWithResultContext {
        val purchaseList = listOf(mockk<Purchase>())
        val purchaseResult = PurchasesResult(BillingResult(), purchaseList)
        mockClientResult {
            every { queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
                val listener = invocation.args[1] as PurchasesResponseListener
                listener.onQueryPurchasesResponse(
                    purchaseResult.billingResult,
                    purchaseResult.purchasesList
                )
            }
        }
        val result = tested.use { it.querySubscriptionPurchases() }
        assertSame(result, purchaseList)
        assertTrue(assertSingleResult("querySubscriptionPurchases").isSuccess)
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

    @Test
    fun `purchase is emitted`() = runTest(testDispatcherProvider.Main) {
        val expectedBillingResult = mockk<BillingResult>()
        val expectedPurchaseList = mockk<List<Purchase>>()

        factory.purchasesUpdatedListener.onPurchasesUpdated(
            expectedBillingResult,
            expectedPurchaseList
        )
        tested.purchaseUpdated.test {
            val (billingResult, purchaseList) = awaitItem()
            assertEquals(expectedBillingResult, billingResult)
            assertEquals(expectedPurchaseList, purchaseList)
        }
    }

    private inline fun <reified R> mockClientResult(billingClientMockSetup: BillingClient.() -> R) {
        val billingClient = mockk<BillingClient> {
            billingClientMockSetup()
        }
        val callbackSlot = slot<suspend (BillingClient) -> R>()
        coEvery { factory.connectedBillingClient.withClient(capture(callbackSlot)) } coAnswers {
            val fn = callbackSlot.captured
            fn(billingClient)
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
    lateinit var purchasesUpdatedListener: PurchasesUpdatedListener

    override fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): ConnectedBillingClient {
        this.purchasesUpdatedListener = purchasesUpdatedListener
        return connectedBillingClient
    }
}
