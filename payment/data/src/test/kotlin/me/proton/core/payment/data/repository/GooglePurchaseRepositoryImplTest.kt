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

package me.proton.core.payment.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.db.dao.GooglePurchaseDao
import me.proton.core.payment.data.local.entity.GooglePurchaseEntity
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GooglePurchaseRepositoryImplTest {

    //region mocks
    @MockK
    private lateinit var googlePurchaseDao: GooglePurchaseDao
    @MockK
    private lateinit var paymentDatabase: PaymentDatabase
    //endregion

    private lateinit var repository: GooglePurchaseRepositoryImpl

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        every { paymentDatabase.googlePurchaseDao() } returns googlePurchaseDao
        repository = GooglePurchaseRepositoryImpl(paymentDatabase)
    }

    @Test
    fun `deleteByGooglePurchaseToken`() = runTest {
        val purchaseTokenString = "test-purchase-token"
        val purchaseToken = GooglePurchaseToken(purchaseTokenString)
        coEvery { googlePurchaseDao.deleteByGooglePurchaseToken(purchaseTokenString) } returns Unit
        repository.deleteByGooglePurchaseToken(purchaseToken)
        coVerify { googlePurchaseDao.deleteByGooglePurchaseToken(purchaseTokenString) }
    }

    @Test
    fun `findGooglePurchaseToken`() = runTest {
        val purchaseTokenString = "test-purchase-token"
        val paymentTokenString = "test-purchase-token"
        val purchaseToken = GooglePurchaseToken(purchaseTokenString)
        val protonPaymentToken = ProtonPaymentToken(paymentTokenString)
        val purchaseEntity = GooglePurchaseEntity(purchaseTokenString, paymentTokenString)
        coEvery { googlePurchaseDao.findByPaymentToken(paymentTokenString) } returns purchaseEntity
        val result = repository.findGooglePurchaseToken(protonPaymentToken)
        coVerify { googlePurchaseDao.findByPaymentToken(paymentTokenString) }
        assertEquals(purchaseToken, result)
    }

    @Test
    fun `updateGooglePurchase`() = runTest {
        val purchaseTokenString = "test-purchase-token"
        val paymentTokenString = "test-purchase-token"
        val purchaseToken = GooglePurchaseToken(purchaseTokenString)
        val protonPaymentToken = ProtonPaymentToken(paymentTokenString)
        val slot = slot<GooglePurchaseEntity>()
        coEvery { googlePurchaseDao.insertOrUpdate(capture(slot)) } returns Unit
        repository.updateGooglePurchase(purchaseToken, protonPaymentToken)
        val captured = slot.captured
        assertEquals(GooglePurchaseEntity(purchaseTokenString, paymentTokenString), captured)
    }
}
