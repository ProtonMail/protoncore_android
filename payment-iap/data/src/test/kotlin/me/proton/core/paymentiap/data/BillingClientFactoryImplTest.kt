package me.proton.core.paymentiap.data

import android.app.Application
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class BillingClientFactoryImplTest {
    @BeforeTest
    fun setUp() {
        mockkStatic(BillingClient::class)
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(BillingClient::class)
    }

    @Test
    fun buildsCorrectBillingClient() {
        // GIVEN
        val app = mockk<Application>()
        val builder = mockk<BillingClient.Builder> {
            every { enablePendingPurchases() } answers { invocation.self as BillingClient.Builder }
            every { setListener(any()) } answers { invocation.self as BillingClient.Builder }
            every { build() } returns mockk()
        }

        every { BillingClient.newBuilder(any()) } returns builder

        // WHEN
        val listener = mockk<PurchasesUpdatedListener>()
        BillingClientFactoryImpl(app).invoke(listener)

        // THEN
        verify(exactly = 1) { builder.enablePendingPurchases() }
        verify(exactly = 1) { builder.setListener(listener) }
    }
}
