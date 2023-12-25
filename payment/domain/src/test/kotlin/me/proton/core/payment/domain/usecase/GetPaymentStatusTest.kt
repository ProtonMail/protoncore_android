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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import kotlin.test.Test

class GetPaymentStatusTest {

    // region mocks
    private var appStore: AppStore = AppStore.GooglePlay

    @MockK
    private lateinit var paymentsRepository: PaymentsRepository
    // endregion

    private lateinit var getPaymentStatus: GetPaymentStatus

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        getPaymentStatus = GetPaymentStatus(appStore, paymentsRepository)
    }

    @Test
    fun `no userid no refresh`() = runTest {
        // GIVEN
        coEvery { paymentsRepository.getPaymentStatus(null, AppStore.GooglePlay) } returns PaymentStatus(
            card = false,
            inApp = true,
            paypal = false
        )
        // WHEN
        getPaymentStatus(userId = null, refresh = false)

        // THEN
        coVerify { paymentsRepository.getPaymentStatus(null, appStore) }
    }

    @Test
    fun `userid no refresh`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        coEvery { paymentsRepository.getPaymentStatus(userId, AppStore.GooglePlay) } returns PaymentStatus(
            card = false,
            inApp = true,
            paypal = false
        )
        // WHEN
        getPaymentStatus(userId = userId, refresh = false)

        // THEN
        coVerify { paymentsRepository.getPaymentStatus(userId, appStore) }
    }

    @Test
    fun `userid refresh`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        coEvery { paymentsRepository.getPaymentStatus(userId, AppStore.GooglePlay) } returns PaymentStatus(
            card = false,
            inApp = true,
            paypal = false
        )
        // WHEN
        getPaymentStatus(userId = userId, refresh = true)

        // THEN
        coVerify { paymentsRepository.getPaymentStatus(userId, appStore) }
    }

    @Test
    fun `userid no refresh verify single call`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        coEvery { paymentsRepository.getPaymentStatus(userId, AppStore.GooglePlay) } returns PaymentStatus(
            card = false,
            inApp = true,
            paypal = false
        )
        // WHEN
        getPaymentStatus(userId = userId, refresh = false)
        getPaymentStatus(userId = userId, refresh = false)

        // THEN
        coVerify(exactly = 1) { paymentsRepository.getPaymentStatus(userId, appStore) }
    }
}