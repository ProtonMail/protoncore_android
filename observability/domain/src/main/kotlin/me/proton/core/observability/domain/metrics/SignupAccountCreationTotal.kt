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

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus

@Serializable
@Schema(description = "Account creation.")
@SchemaId("https://proton.me/android_core_signup_accountCreation_total_v3.schema.json")
public data class SignupAccountCreationTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : ObservabilityData() {
    public constructor(status: ApiStatus, accountType: Type) : this(LabelsData(status, accountType))

    public constructor(result: Result<*>, accountType: Type) : this(result.toApiStatus(), accountType)

    @Serializable
    public data class LabelsData constructor(
        @get:Schema(required = true)
        val status: ApiStatus,

        @get:Schema(required = true)
        val accountType: Type
    )

    @Suppress("EnumEntryName", "EnumNaming")
    public enum class Type {
        proton,
        external
    }

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ApiStatus {
        http2xx,
        http409UsernameConflict,
        http422HvRequired,
        http4xx,
        http5xx,
        connectionError,
        notConnected,
        parseError,
        sslError,
        unknown
    }
}

private fun Result<*>.toApiStatus(): SignupAccountCreationTotal.ApiStatus = when {
    isHvRequiredError() -> SignupAccountCreationTotal.ApiStatus.http422HvRequired
    isUsernameConflictError() -> SignupAccountCreationTotal.ApiStatus.http409UsernameConflict
    else -> toHttpApiStatus().toAccountCreationApiStatus()
}

private fun Result<*>.isHvRequiredError(): Boolean =
    toProtonErrorCode(HttpResponseCodes.HTTP_UNPROCESSABLE) == ResponseCodes.HUMAN_VERIFICATION_REQUIRED

private fun Result<*>.isUsernameConflictError(): Boolean =
    toProtonErrorCode(HttpResponseCodes.HTTP_CONFLICT) == ResponseCodes.NOT_ALLOWED

private fun Result<*>.toProtonErrorCode(filterByHttpCode: Int): Int? =
    ((exceptionOrNull() as? ApiException)?.error as? ApiResult.Error.Http)
        ?.takeIf { it.httpCode == filterByHttpCode }
        ?.proton
        ?.code

private fun HttpApiStatus.toAccountCreationApiStatus(): SignupAccountCreationTotal.ApiStatus =
    when (this) {
        HttpApiStatus.http2xx -> SignupAccountCreationTotal.ApiStatus.http2xx
        HttpApiStatus.http4xx -> SignupAccountCreationTotal.ApiStatus.http4xx
        HttpApiStatus.http5xx -> SignupAccountCreationTotal.ApiStatus.http5xx
        HttpApiStatus.connectionError -> SignupAccountCreationTotal.ApiStatus.connectionError
        HttpApiStatus.notConnected -> SignupAccountCreationTotal.ApiStatus.notConnected
        HttpApiStatus.parseError -> SignupAccountCreationTotal.ApiStatus.parseError
        HttpApiStatus.sslError -> SignupAccountCreationTotal.ApiStatus.sslError
        HttpApiStatus.unknown -> SignupAccountCreationTotal.ApiStatus.unknown
    }
