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

package me.proton.core.payment.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.Currency
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreatePaymentTokenWithGoogleIAPTest {
    private lateinit var tested: CreatePaymentTokenWithGoogleIAP

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = CreatePaymentTokenWithGoogleIAP(mockk(relaxed = true), mockk(relaxed = true), observabilityManager)
    }

    @Test
    fun `observability metrics are recorded`() = runTest {
        tested(
            userId = null,
            amount = 1,
            currency = Currency.CHF,
            paymentType = mockk(relaxed = true),
            metricData = { CheckoutGiapBillingCreatePaymentTokenTotal(it.toHttpApiStatus()) }
        )
    }
}
