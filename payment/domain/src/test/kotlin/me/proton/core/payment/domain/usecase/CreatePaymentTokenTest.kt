/*
 * Copyright (c) 2025 Proton AG
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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.repository.PaymentsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreatePaymentTokenTest {

    private val paymentRepository = mockk<PaymentsRepository>(relaxed = true)
    private val googlePurchaseRepository = mockk<GooglePurchaseRepository>(relaxed = true)

    private lateinit var tested: CreatePaymentToken

    @BeforeTest
    fun setUp() {
        tested = CreatePaymentToken(paymentRepository, googlePurchaseRepository)
    }

    @Test
    fun `call createPaymentToken for PayPal`() = runTest {
        // GIVEN
        val userId: UserId? = null
        val type = PaymentType.PayPal
        // WHEN
        tested(userId = userId, paymentType = type)
        // THEN
        coVerify {
            paymentRepository.createPaymentToken(sessionUserId = userId, paymentType = type)
        }
    }

    @Test
    fun `call createPaymentToken for CreditCard`() = runTest {
        // GIVEN
        val userId: UserId? = null
        val type: PaymentType.CreditCard = mockk()
        // WHEN
        tested(userId = userId, paymentType = type)
        // THEN
        coVerify {
            paymentRepository.createPaymentToken(sessionUserId = userId, paymentType = type)
        }
    }

    @Test
    fun `call createPaymentToken for PaymentMethod`() = runTest {
        // GIVEN
        val userId: UserId? = null
        val type: PaymentType.PaymentMethod = mockk()
        // WHEN
        tested(userId = userId, paymentType = type)
        // THEN
        coVerify {
            paymentRepository.createPaymentToken(sessionUserId = userId, paymentType = type)
        }
    }

    @Test
    fun `call createPaymentToken for GoogleIAP`() = runTest {
        // GIVEN
        val userId: UserId? = null
        val type = PaymentType.GoogleIAP(
            productId = "productId",
            purchaseToken = GooglePurchaseToken("token"),
            orderId = "orderId",
            packageName = "packageName",
            customerId = "customerId"
        )
        val result = PaymentTokenResult.CreatePaymentTokenResult(
            status = PaymentTokenStatus.CHARGEABLE,
            token = ProtonPaymentToken("token"),
            approvalUrl = null,
            returnHost = null
        )
        coEvery { paymentRepository.createPaymentToken(any(), any()) } returns result
        // WHEN
        tested(userId = userId, paymentType = type)
        // THEN
        coVerify {
            paymentRepository.createPaymentToken(sessionUserId = userId, paymentType = type)
            googlePurchaseRepository.updateGooglePurchase(type.purchaseToken, result.token)
        }
    }
}
