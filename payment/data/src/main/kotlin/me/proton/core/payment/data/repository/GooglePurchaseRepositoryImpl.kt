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

import me.proton.core.payment.data.local.db.PaymentDatabase
import me.proton.core.payment.data.local.entity.GooglePurchaseEntity
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import javax.inject.Inject

public class GooglePurchaseRepositoryImpl @Inject constructor(
    paymentDatabase: PaymentDatabase
) : GooglePurchaseRepository {
    private val dao = paymentDatabase.googlePurchaseDao()

    override suspend fun deleteByGooglePurchaseToken(googlePurchaseToken: GooglePurchaseToken) {
        dao.deleteByGooglePurchaseToken(googlePurchaseToken.value)
    }

    override suspend fun deleteByProtonPaymentToken(paymentToken: ProtonPaymentToken) {
        dao.deleteByProtonPaymentToken(paymentToken.value)
    }

    override suspend fun findGooglePurchaseToken(paymentToken: ProtonPaymentToken): GooglePurchaseToken? {
        val entity = dao.findByPaymentToken(paymentToken.value)
        return entity?.googlePurchaseToken?.let { GooglePurchaseToken(it) }
    }

    override suspend fun updateGooglePurchase(
        googlePurchaseToken: GooglePurchaseToken,
        paymentToken: ProtonPaymentToken
    ) {
        val entity = GooglePurchaseEntity(
            googlePurchaseToken = googlePurchaseToken.value,
            paymentToken = paymentToken.value
        )
        dao.insertOrUpdate(entity)
    }
}
