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

package me.proton.core.paymentiap.data.repository

import app.cash.turbine.test
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.metrics.common.GiapStatus
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.paymentiap.domain.LogTag
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.toGiapStatus
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.runTestWithResultContext
import me.proton.core.util.kotlin.CoreLogger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GoogleBillingRepositoryImplTest {
    private lateinit var billingClientFactory: FakeBillingClientFactory
    private lateinit var factory: FakeConnectedBillingClientFactory
    private lateinit var testDispatcherProvider: TestDispatcherProvider
    private lateinit var tested: GoogleBillingRepositoryImpl

    private fun mockPurchase(): Purchase = mockk<Purchase> {
        every { accountIdentifiers } returns null
        every { orderId } returns "orderId"
        every { packageName } returns "packageName"
        every { purchaseState } returns PurchaseState.PURCHASED
        every { purchaseTime } returns 0
        every { purchaseToken } returns "token"
        every { products } returns listOf("product1")
        every { isAcknowledged } returns false
    }

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
                    .setResponseCode(BillingResponseCode.ERROR)
                    .build()
                listener.onAcknowledgePurchaseResponse(result)
            }
        }
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }
    }

    @Test
    fun `acknowledge a purchase after retry`() = runTest {
        // GIVEN
        var callCount = 0
        val billingClient = mockClientResult {
            every { acknowledgePurchase(any(), any()) } answers {
                callCount += 1
                val listener = invocation.args[1] as AcknowledgePurchaseResponseListener
                val responseCode = when (callCount) {
                    1 -> BillingResponseCode.SERVICE_UNAVAILABLE
                    else -> BillingResponseCode.OK
                }
                val result = BillingResult.newBuilder()
                    .setResponseCode(responseCode)
                    .build()
                listener.onAcknowledgePurchaseResponse(result)
            }
        }

        // WHEN
        tested.use { it.acknowledgePurchase(GooglePurchaseToken("token-123")) }

        // THEN
        verify(exactly = 2) { billingClient.acknowledgePurchase(any(), any()) }
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
        val result = tested.use {
            it.getProductsDetails(listOf(ProductId("plan-name")))
        }?.map { it.unwrap() }
        assertEquals(result, listOf(productDetails))
    }

    @Test
    fun `get product details null`() = runTestWithResultContext {
        mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                listener.onProductDetailsResponse(BillingResult(), listOf())
            }
            every { connectionState } returns BillingClient.ConnectionState.CONNECTED
            every { isFeatureSupported(any()) } returns BillingResult()
        }
        val result = tested.use { it.getProductsDetails(listOf(ProductId("plan-name"))) }
        assertTrue(result?.isEmpty() == true)
        assertTrue(assertSingleResult("getProductsDetails").isSuccess)
    }

    @Test
    fun `get product details error`() = runTestWithResultContext {
        mockkObject(CoreLogger)
        mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                listener.onProductDetailsResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.ERROR)
                        .setDebugMessage("error")
                        .build(), listOf()
                )
            }
        }
        val exception = assertFailsWith<BillingClientError> {
            tested.use { it.getProductsDetails(listOf(ProductId("plan-name"))) }
        }
        assertSame("error", exception.debugMessage)
        assertEquals(BillingResponseCode.ERROR, exception.responseCode)

        val result = assertSingleResult("getProductsDetails")
        assertTrue(result.isFailure)
        val status = result.toGiapStatus()
        assertEquals(GiapStatus.googlePlayError, status)
        verify(exactly = 1) { CoreLogger.e(LogTag.GIAP_ERROR, exception) }

        unmockkObject(CoreLogger)
    }

    @Test
    fun `get product details error with retry`() = runTestWithResultContext {
        // GIVEN
        mockkObject(CoreLogger)

        var callCount = 0
        val billingClient = mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                callCount += 1
                if (callCount == 1) {
                    listener.onProductDetailsResponse(
                        BillingResult.newBuilder()
                            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                            .setDebugMessage("error")
                            .build(), listOf()
                    )
                } else {
                    listener.onProductDetailsResponse(BillingResult(), listOf())
                }
            }
            every { connectionState } returns BillingClient.ConnectionState.CONNECTED
            every { isFeatureSupported(any()) } returns BillingResult()
        }

        // WHEN
        val result = tested.use { it.getProductsDetails(listOf(ProductId("plan-name"))) }

        // THEN
        assertTrue(result?.isEmpty() == true)
        assertTrue(assertSingleResult("getProductsDetails").isSuccess)

        verify(exactly = 2) { billingClient.queryProductDetailsAsync(any(), any()) }

        unmockkObject(CoreLogger)
    }

    @Test
    fun `get product details no error`() = runTestWithResultContext {
        mockkObject(CoreLogger)
        mockClientResult {
            every { queryProductDetailsAsync(any(), any()) } answers {
                val listener = invocation.args[1] as ProductDetailsResponseListener
                listener.onProductDetailsResponse(
                    BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK)
                        .setDebugMessage("no error")
                        .build(), listOf()
                )
            }
            every { connectionState } returns BillingClient.ConnectionState.CONNECTED
            every { isFeatureSupported(any()) } returns BillingResult()
        }
        val result = tested.use { it.getProductsDetails(listOf(ProductId("plan-name"))) }
        assertTrue(result?.isEmpty() == true)
        assertTrue(assertSingleResult("getProductsDetails").isSuccess)
        verify(exactly = 0) { CoreLogger.e(LogTag.GIAP_ERROR, any(), any()) }
        unmockkObject(CoreLogger)
    }

    @Test
    fun `launch billing flow`() = runTestWithResultContext {
        mockClientResult {
            every { launchBillingFlow(any(), any()) } returns BillingResult()
        }
        tested.use { it.launchBillingFlow(mockk(relaxed = true)) } //mockk<BillingFlowParams>().wrap()
        assertTrue(assertSingleResult("launchBillingFlow").isSuccess)
    }

    @Test
    fun `query subscription purchases`() = runTestWithResultContext {
        val purchaseList = listOf(mockPurchase())
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
        val result = tested.use { it.querySubscriptionPurchases() }.map { it.unwrap() }
        assertContentEquals(result, purchaseList)
        assertTrue(assertSingleResult("querySubscriptionPurchases").isSuccess)
    }

    @Test
    fun `query subscription purchases after retry`() = runTestWithResultContext {
        // GIVEN
        val purchaseList = listOf(mockPurchase())
        val purchaseResult = PurchasesResult(BillingResult(), purchaseList)
        var callCount = 0
        val billingClient = mockClientResult {
            every { queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
                callCount += 1
                val listener = invocation.args[1] as PurchasesResponseListener
                if (callCount == 1) {
                    listener.onQueryPurchasesResponse(
                        BillingResult.newBuilder()
                            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                            .setDebugMessage("error")
                            .build(),
                        emptyList()
                    )
                } else {
                    listener.onQueryPurchasesResponse(
                        purchaseResult.billingResult,
                        purchaseResult.purchasesList
                    )
                }
            }
        }

        // WHEN
        val result = tested.use { it.querySubscriptionPurchases() }.map { it.unwrap() }

        // THEN
        assertContentEquals(result, purchaseList)
        assertTrue(assertSingleResult("querySubscriptionPurchases").isSuccess)
        verify(exactly = 2) { billingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) }
    }

    @Test
    fun `query subscription purchases yielding`() = runTestWithResultContext {
        val purchaseList = listOf(mockPurchase())
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
        val result = tested.use { it.querySubscriptionPurchases() }.map { it.unwrap() }
        assertContentEquals(result, purchaseList)
        assertTrue(assertSingleResult("querySubscriptionPurchases").isSuccess)
    }

    @Test
    fun `purchase is emitted`() = runTest(testDispatcherProvider.Main) {
        val expectedBillingResult = mockk<BillingResult>()
        val expectedPurchaseList = listOf(mockPurchase(), mockPurchase())

        factory.purchasesUpdatedListener.onPurchasesUpdated(
            expectedBillingResult,
            expectedPurchaseList
        )
        tested.purchaseUpdated.test {
            val (billingResult, purchaseList) = awaitItem()
            assertEquals(expectedBillingResult, billingResult.unwrap())
            assertEquals(expectedPurchaseList, purchaseList?.map { it.unwrap() })
        }
    }

    @Test
    fun `close`() = runTest {
        val googleBillingRepositorySpy = spyk(tested)

        googleBillingRepositorySpy.close()
        verify { googleBillingRepositorySpy.destroy() }
    }

    private inline fun <reified R> mockClientResult(billingClientMockSetup: BillingClient.() -> R): BillingClient {
        val billingClient = mockk<BillingClient> {
            billingClientMockSetup()
        }
        val callbackSlot = slot<suspend (BillingClient) -> R>()
        coEvery { factory.connectedBillingClient.withClient(capture(callbackSlot)) } coAnswers {
            val fn = callbackSlot.captured
            fn(billingClient)
        }
        return billingClient
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
