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
        coEvery { googlePurchaseRepository.findGooglePurchaseToken("payment-token") } returns "google-purchase-token"
        tested("payment-token")

        coVerify { googleBillingRepository.acknowledgePurchase("google-purchase-token") }
        verify { googleBillingRepository.close() }
        coVerify { googlePurchaseRepository.deleteByGooglePurchaseToken("google-purchase-token") }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fails to find a corresponding payment token`() = runBlockingTest {
        coEvery { googlePurchaseRepository.findGooglePurchaseToken("payment-token") } returns null
        tested("payment-token")
    }
}