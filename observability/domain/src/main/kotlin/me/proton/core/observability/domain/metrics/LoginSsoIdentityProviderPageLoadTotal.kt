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
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Web view page loads for SSO identity providers.")
@SchemaId("https://proton.me/android_core_login_ssoIdentityProvider_pageLoad_total_v1.schema.json")
public data class LoginSsoIdentityProviderPageLoadTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1,
) : CoreObservabilityData() {

    public constructor(errorCode: Int?) : this(LabelsData(errorCode.toStatus()))

    @Serializable
    public data class LabelsData constructor(
        val status: Status
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class Status {
        http2xx,
        http4xx,
        http5xx,
        connectionError,
        sslError
    }
}

private fun Int?.toStatus(): LoginSsoIdentityProviderPageLoadTotal.Status = when (this) {
    null -> LoginSsoIdentityProviderPageLoadTotal.Status.http2xx
    in 200..299 -> LoginSsoIdentityProviderPageLoadTotal.Status.http2xx
    in 400..499 -> LoginSsoIdentityProviderPageLoadTotal.Status.http4xx
    in 500..599 -> LoginSsoIdentityProviderPageLoadTotal.Status.http5xx
    else -> LoginSsoIdentityProviderPageLoadTotal.Status.connectionError
}
