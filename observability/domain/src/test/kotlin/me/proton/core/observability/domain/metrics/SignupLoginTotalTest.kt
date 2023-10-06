/*
 * Copyright (c) 2023 Proton AG
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

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.domain.metrics.SignupLoginTotal.ApiStatus
import me.proton.core.observability.domain.metrics.common.AccountTypeLabels
import kotlin.test.Test
import kotlin.test.assertEquals

class SignupLoginTotalTest {
    @Test
    fun `result to http2xx`() {
        val data = SignupLoginTotal(Result.success("ok"), AccountType.Internal)
        assertEquals(AccountTypeLabels.internal, data.Labels.accountType)
        assertEquals(ApiStatus.http2xx, data.Labels.status)
    }

    @Test
    fun `result to http401`() {
        assertEquals(
            ApiStatus.http401,
            SignupLoginTotal(makeHttpFailureResult(401), AccountType.Internal).Labels.status
        )
    }

    @Test
    fun `result to http422`() {
        assertEquals(
            ApiStatus.http422,
            SignupLoginTotal(makeHttpFailureResult(422), AccountType.Internal).Labels.status
        )

        assertEquals(
            ApiStatus.http422_2001InvalidValue,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.INVALID_VALUE),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_2028Banned,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.BANNED),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_8002PasswordWrong,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.PASSWORD_WRONG),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_9001HvRequired,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.HUMAN_VERIFICATION_REQUIRED),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_9002DvRequired,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.DEVICE_VERIFICATION_REQUIRED),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_10001AccountFailedGeneric,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.ACCOUNT_FAILED_GENERIC),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_10002AccountDeleted,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.ACCOUNT_DELETED),
                AccountType.Internal
            ).Labels.status
        )

        assertEquals(
            ApiStatus.http422_10003AccountDisabled,
            SignupLoginTotal(
                makeHttpFailureResult(422, ResponseCodes.ACCOUNT_DISABLED),
                AccountType.Internal
            ).Labels.status
        )
    }

    @Test
    fun `result to http429`() {
        assertEquals(
            ApiStatus.http429,
            SignupLoginTotal(makeHttpFailureResult(429), AccountType.Internal).Labels.status
        )

        assertEquals(
            ApiStatus.http429_2028Banned,
            SignupLoginTotal(
                makeHttpFailureResult(429, ResponseCodes.BANNED),
                AccountType.Internal
            ).Labels.status
        )
    }

    private fun makeHttpFailureResult(
        httpCode: Int,
        protonCode: Int? = null
    ): Result<Any> = Result.failure(
        ApiException(
            ApiResult.Error.Http(
                httpCode,
                "msg",
                protonCode?.let { ApiResult.Error.ProtonData(protonCode, "Error $protonCode") })
        )
    )
}
