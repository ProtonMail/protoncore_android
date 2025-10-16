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

package me.proton.core.payment.domain

import kotlinx.coroutines.CancellationException
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.payment.domain.entity.GoogleBillingResponseCode
import me.proton.core.payment.domain.repository.BillingClientError

/**
 * For use with an error caught in a [Result]. This is scoped to the context of Google In App
 * Purchase, and or networking events.
 *
 * @return whether the error is recoverable or not. True denotes the [Result] should be retried.
 */
public fun Throwable.isRecoverable(): Boolean {
    return when (this) {
        is CancellationException -> {
            true
        }
        is ApiException -> {
            isRetryable()
        }
        is BillingClientError -> {
            when (responseCode) {
                GoogleBillingResponseCode.ERROR,
                GoogleBillingResponseCode.ITEM_ALREADY_OWNED,
                GoogleBillingResponseCode.ITEM_NOT_OWNED,
                GoogleBillingResponseCode.NETWORK_ERROR,
                GoogleBillingResponseCode.SERVICE_DISCONNECTED,
                GoogleBillingResponseCode.SERVICE_UNAVAILABLE -> {
                    true
                }
                else -> {
                    false
                }
            }
        }
        else -> {
            false
        }
    }
}