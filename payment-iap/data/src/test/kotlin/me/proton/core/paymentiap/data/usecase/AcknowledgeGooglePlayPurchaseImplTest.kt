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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class AcknowledgeGooglePlayPurchaseImplTest {
    private lateinit var googleBillingRepository: GoogleBillingRepository
    private lateinit var googlePurchaseRepository: GooglePurchaseRepository
    private lateinit var tested: AcknowledgeGooglePlayPurchaseImpl

    @BeforeTest
    fun setUp() {
        googleBillingRepository = mockk(relaxed = true)
        googlePurchaseRepository = mockk(relaxed = true)
        tested = AcknowledgeGooglePlayPurchaseImpl({ googleBillingRepository }, googlePurchaseRepository)
    }

    @Test
    fun `acknowledges a purchase`() = runBlockingTest {
        val paymentToken = ProtonPaymentToken("payment-token")
        val purchaseToken = GooglePurchaseToken("google-purchase-token")
        coEvery { googlePurchaseRepository.findGooglePurchaseToken(paymentToken) } returns purchaseToken
        tested(paymentToken)

        coVerify { googleBillingRepository.acknowledgePurchase(purchaseToken) }
        verify { googleBillingRepository.close() }
        coVerify { googlePurchaseRepository.deleteByGooglePurchaseToken(purchaseToken) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fails to find a corresponding payment token`() = runBlockingTest {
        val paymentToken = ProtonPaymentToken("payment-token")
        coEvery { googlePurchaseRepository.findGooglePurchaseToken(paymentToken) } returns null
        tested(paymentToken)
    }
}