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

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.network.domain.HttpResponseCodes.HTTP_TOO_MANY_REQUESTS
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNAUTHORIZED
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.ResponseCodes.ACCOUNT_DELETED
import me.proton.core.network.domain.ResponseCodes.ACCOUNT_DISABLED
import me.proton.core.network.domain.ResponseCodes.ACCOUNT_FAILED_GENERIC
import me.proton.core.network.domain.ResponseCodes.BANNED
import me.proton.core.network.domain.ResponseCodes.DEVICE_VERIFICATION_REQUIRED
import me.proton.core.network.domain.ResponseCodes.HUMAN_VERIFICATION_REQUIRED
import me.proton.core.network.domain.ResponseCodes.INVALID_VALUE
import me.proton.core.network.domain.ResponseCodes.PASSWORD_WRONG
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.SignupLoginTotal.ApiStatus
import me.proton.core.observability.domain.metrics.common.AccountTypeLabels
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.hasProtonErrorCode
import me.proton.core.observability.domain.metrics.common.isHttpError
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.observability.domain.metrics.common.toObservabilityAccountType

@Serializable
@Schema(description = "Logging in just after the signup.")
@SchemaId("https://proton.me/android_core_signup_login_total_v4.schema.json")
public data class SignupLoginTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : CoreObservabilityData() {
    public constructor(status: ApiStatus, type: AccountTypeLabels) : this(LabelsData(status, type))

    public constructor(result: Result<*>, accountType: AccountType) : this(
        result.toApiStatus(),
        accountType.toObservabilityAccountType()
    )

    @Serializable
    public data class LabelsData constructor(
        @get:Schema(required = true)
        val status: ApiStatus,

        @get:Schema(required = true)
        val accountType: AccountTypeLabels
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
        http1xx,
        http2xx,
        http3xx,
        http401,
        http422_2001InvalidValue,
        http422_2028Banned,
        http422_8002PasswordWrong,
        http422_9001HvRequired,
        http422_9002DvRequired,
        http422_10001AccountFailedGeneric,
        http422_10002AccountDeleted,
        http422_10003AccountDisabled,
        http422,
        http429_2028Banned,
        http429,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        cancellation,
        unknown
    }
}

private fun Result<*>.toApiStatus(): ApiStatus = when {
    isHttpError(HTTP_UNAUTHORIZED) -> ApiStatus.http401
    isHttpError(HTTP_UNPROCESSABLE) -> when {
        hasProtonErrorCode(INVALID_VALUE) -> ApiStatus.http422_2001InvalidValue
        hasProtonErrorCode(BANNED) -> ApiStatus.http422_2028Banned
        hasProtonErrorCode(PASSWORD_WRONG) -> ApiStatus.http422_8002PasswordWrong
        hasProtonErrorCode(HUMAN_VERIFICATION_REQUIRED) -> ApiStatus.http422_9001HvRequired
        hasProtonErrorCode(DEVICE_VERIFICATION_REQUIRED) -> ApiStatus.http422_9002DvRequired
        hasProtonErrorCode(ACCOUNT_FAILED_GENERIC) -> ApiStatus.http422_10001AccountFailedGeneric
        hasProtonErrorCode(ACCOUNT_DELETED) -> ApiStatus.http422_10002AccountDeleted
        hasProtonErrorCode(ACCOUNT_DISABLED) -> ApiStatus.http422_10003AccountDisabled
        else -> ApiStatus.http422
    }

    isHttpError(HTTP_TOO_MANY_REQUESTS) -> when {
        hasProtonErrorCode(BANNED) -> ApiStatus.http429_2028Banned
        else -> ApiStatus.http429
    }

    else -> toHttpApiStatus().toSignupLoginTotalApiStatus()
}

private fun HttpApiStatus.toSignupLoginTotalApiStatus(): ApiStatus = when (this) {
    HttpApiStatus.http1xx -> ApiStatus.http1xx
    HttpApiStatus.http2xx -> ApiStatus.http2xx
    HttpApiStatus.http3xx -> ApiStatus.http3xx
    HttpApiStatus.http4xx -> ApiStatus.http4xx
    HttpApiStatus.http5xx -> ApiStatus.http5xx
    HttpApiStatus.connectionError -> ApiStatus.connectionError
    HttpApiStatus.notConnected -> ApiStatus.notConnected
    HttpApiStatus.parseError -> ApiStatus.parseError
    HttpApiStatus.sslError -> ApiStatus.sslError
    HttpApiStatus.cancellation -> ApiStatus.cancellation
    HttpApiStatus.unknown -> ApiStatus.unknown
}
