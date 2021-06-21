/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

/**
 * Gets current active subscription a user has.
 * For free users this will return Null.
 * Authorized. This means that it could only be used for upgrades. New accounts created during sign ups logically do not
 * have existing subscriptions.
 */
class GetCurrentSubscription @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    suspend operator fun invoke(userId: UserId): Subscription? {
        return try {
            paymentsRepository.getSubscription(userId)
        } catch (exception: ApiException) {
            val error = exception.error
            if (error is ApiResult.Error.Http && error.proton?.code == NO_ACTIVE_SUBSCRIPTION) {
                return null
            } else {
                throw exception
            }
        }
    }

    companion object {
        const val NO_ACTIVE_SUBSCRIPTION = 22110
    }
}
