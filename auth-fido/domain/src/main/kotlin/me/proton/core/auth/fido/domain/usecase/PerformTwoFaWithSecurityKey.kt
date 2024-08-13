/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.fido.domain.usecase

import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.observability.domain.metrics.common.FidoLaunchStatus
import me.proton.core.observability.domain.metrics.common.FidoSignStatus

/**
 * Performs 2FA using a FIDO2 security key.
 * The caller needs to call [register] first.
 * When 2FA is requested, call [invoke] and check the launch result.
 * The final result will be delivered to the callback given in the call to [register].
 */
public interface PerformTwoFaWithSecurityKey<T: Any, A : Any> {
    public suspend operator fun invoke(
        activity: A,
        publicKey: Fido2PublicKeyCredentialRequestOptions
    ): LaunchResult

    public fun register(caller: T, onResult: (Result, Fido2PublicKeyCredentialRequestOptions) -> Unit)

    public sealed class LaunchResult {
        public data object Success : LaunchResult()
        public data class Failure(val exception: Exception) : LaunchResult()
    }

    public sealed class Result {
        public data object Cancelled : Result()
        public data class Error(
            val error: ErrorData
        ) : Result()

        public class Success(
            public val rawId: ByteArray,
            public val authenticatorAttachment: String,
            public val type: String,
            public val id: String,
            public val response: SuccessResponseData
        ) : Result()

        public data object EmptyResult : Result()

        public data object UnknownResult : Result()
        public data class NoCredentialsResponse(
            val error: Throwable
        ) : Result()
    }

    public data class ErrorData(
        val code: ErrorCode,
        val message: String?
    )

    /**
     * Based on `com.google.android.gms.fido.fido2.api.common.ErrorCode`.
     */
    @Suppress("MagicNumber")
    public enum class ErrorCode(public val code: Int?) {
        NOT_SUPPORTED_ERR(9),
        INVALID_STATE_ERR(11),
        SECURITY_ERR(18),
        NETWORK_ERR(19),
        ABORT_ERR(20),
        TIMEOUT_ERR(23),
        ENCODING_ERR(27),
        UNKNOWN_ERR(28),
        CONSTRAINT_ERR(29),
        DATA_ERR(30),
        NOT_ALLOWED_ERR(35),
        ATTESTATION_NOT_PRIVATE_ERR(36);
    }

    public class SuccessResponseData(
        public val clientDataJSON: ByteArray,
        public val authenticatorData: ByteArray,
        public val signature: ByteArray
    )
}

public fun PerformTwoFaWithSecurityKey.LaunchResult.toFidoStatus(): FidoLaunchStatus = when (this) {
    is PerformTwoFaWithSecurityKey.LaunchResult.Failure -> FidoLaunchStatus.failure
    is PerformTwoFaWithSecurityKey.LaunchResult.Success -> FidoLaunchStatus.success
}

public fun PerformTwoFaWithSecurityKey.Result.toFidoStatus(): FidoSignStatus = when (this) {
    is PerformTwoFaWithSecurityKey.Result.Cancelled -> FidoSignStatus.userCancelled
    is PerformTwoFaWithSecurityKey.Result.EmptyResult -> FidoSignStatus.empty
    is PerformTwoFaWithSecurityKey.Result.Error -> when (error.code) {
        PerformTwoFaWithSecurityKey.ErrorCode.NOT_SUPPORTED_ERR -> FidoSignStatus.failureNotSupported
        PerformTwoFaWithSecurityKey.ErrorCode.INVALID_STATE_ERR -> FidoSignStatus.failureInvalidState
        PerformTwoFaWithSecurityKey.ErrorCode.SECURITY_ERR -> FidoSignStatus.failureSecurity
        PerformTwoFaWithSecurityKey.ErrorCode.NETWORK_ERR -> FidoSignStatus.failureNetwork
        PerformTwoFaWithSecurityKey.ErrorCode.ABORT_ERR -> FidoSignStatus.failureAbort
        PerformTwoFaWithSecurityKey.ErrorCode.TIMEOUT_ERR -> FidoSignStatus.failureTimeout
        PerformTwoFaWithSecurityKey.ErrorCode.ENCODING_ERR -> FidoSignStatus.failureEncoding
        PerformTwoFaWithSecurityKey.ErrorCode.CONSTRAINT_ERR -> FidoSignStatus.failureConstraint
        PerformTwoFaWithSecurityKey.ErrorCode.DATA_ERR -> FidoSignStatus.failureData
        PerformTwoFaWithSecurityKey.ErrorCode.NOT_ALLOWED_ERR -> FidoSignStatus.failureNotAllowed
        PerformTwoFaWithSecurityKey.ErrorCode.ATTESTATION_NOT_PRIVATE_ERR -> FidoSignStatus.failureAttestationNotPrivate
        PerformTwoFaWithSecurityKey.ErrorCode.UNKNOWN_ERR -> FidoSignStatus.failureUnknown
    }

    is PerformTwoFaWithSecurityKey.Result.Success -> FidoSignStatus.success
    is PerformTwoFaWithSecurityKey.Result.UnknownResult -> FidoSignStatus.unknown
    is PerformTwoFaWithSecurityKey.Result.NoCredentialsResponse -> FidoSignStatus.failureNoResponse
}
