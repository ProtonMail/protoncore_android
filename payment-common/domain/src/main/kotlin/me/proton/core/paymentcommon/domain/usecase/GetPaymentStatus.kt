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

package me.proton.core.paymentcommon.domain.usecase

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.paymentcommon.domain.entity.PaymentStatus
import me.proton.core.paymentcommon.domain.repository.PaymentsRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Gets current (cached) status for payments.
 * @see GetAvailablePaymentProviders
 */
@Singleton
internal class GetPaymentStatus @Inject constructor(
    private val appStore: AppStore,
    private val paymentsRepository: PaymentsRepository
) {
    private val paymentStatusCache = mutableMapOf<UserId?, PaymentStatus>()
    private val paymentStatusMutex = Mutex()

    /** Returns current status for payments.
     * The status may be cached in memory.
     * @throws me.proton.core.network.domain.ApiException
     */
    suspend operator fun invoke(userId: UserId?, refresh: Boolean): PaymentStatus = paymentStatusMutex.withLock {
        if (refresh) paymentStatusCache.remove(userId)

        paymentStatusCache.getOrPut(userId) {
            paymentsRepository.getPaymentStatus(userId, appStore)
        }
    }
}
