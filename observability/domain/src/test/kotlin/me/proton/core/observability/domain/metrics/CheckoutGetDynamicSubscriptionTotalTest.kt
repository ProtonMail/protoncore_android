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

package me.proton.core.observability.domain.metrics

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import org.junit.Test
import kotlin.test.assertEquals

class CheckoutGetDynamicSubscriptionTotalTest {

    @Test
    fun httpSuccess() {
        val result = Result.success(Unit).toGetDynamicSubscriptionApiStatus()
        assertEquals(CheckoutGetDynamicSubscriptionTotal.ApiStatus.http2xx, result)
    }

    @Test
    fun notConnectedError() {
        val result = Result.failure<Any>(ApiException(ApiResult.Error.Connection(isConnectedToNetwork = false)))
            .toGetDynamicSubscriptionApiStatus()
        assertEquals(CheckoutGetDynamicSubscriptionTotal.ApiStatus.notConnected, result)
    }

    @Test
    fun notProcessableError() {
        val result =
            Result.failure<Any>(ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_UNPROCESSABLE, "422")))
                .toGetDynamicSubscriptionApiStatus()
        assertEquals(CheckoutGetDynamicSubscriptionTotal.ApiStatus.http422, result)
    }

    @Test
    fun conflictError() {
        val result =
            Result.failure<Any>(ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_CONFLICT, "409")))
                .toGetDynamicSubscriptionApiStatus()
        assertEquals(CheckoutGetDynamicSubscriptionTotal.ApiStatus.http409, result)
    }

    @Test
    fun constructorConflictError() {
        val event = CheckoutGetDynamicSubscriptionTotal(
            Result.failure<Any>(ApiException(ApiResult.Error.Http(HttpResponseCodes.HTTP_CONFLICT, "409")))
        )
        assertEquals(CheckoutGetDynamicSubscriptionTotal.ApiStatus.http409, event.Labels.status)
    }
}