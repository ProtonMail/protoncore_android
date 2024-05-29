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

package me.proton.core.auth.fido.play.usecase

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.common.Transport
import com.google.android.gms.fido.fido2.api.common.AuthenticationExtensions
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.ErrorCode
import com.google.android.gms.fido.fido2.api.common.FidoAppIdExtension
import com.google.android.gms.fido.fido2.api.common.GoogleThirdPartyPaymentExtension
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions
import com.google.android.gms.fido.fido2.api.common.UserVerificationMethodExtension
import kotlinx.coroutines.suspendCancellableCoroutine
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.ErrorCode as ProtonErrorCode
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

public class PerformTwoFaWithSecurityKeyImpl @Inject constructor() : PerformTwoFaWithSecurityKey<ComponentActivity> {
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    /**
     * The options that the client received from BE.
     * The client will need it later, when it sends an answer to BE.
     */
    private var publicKeyOptions: Fido2PublicKeyCredentialRequestOptions? = null

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun invoke(
        activity: ComponentActivity,
        publicKey: Fido2PublicKeyCredentialRequestOptions
    ): PerformTwoFaWithSecurityKey.LaunchResult {
        publicKeyOptions = publicKey

        val fidoClient = Fido.getFido2ApiClient(activity)
        val rpId = requireNotNull(publicKey.rpId)
        val options = PublicKeyCredentialRequestOptions.Builder()
            .setAllowList(
                publicKey.allowCredentials?.map { descriptor ->
                    PublicKeyCredentialDescriptor(
                        descriptor.type,
                        descriptor.id.toByteArray(),
                        descriptor.transports?.map { Transport.fromString(it) }
                    )
                }
            )
            .apply {
                if (publicKey.hasExtensions()) {
                    setAuthenticationExtensions(AuthenticationExtensions.Builder()
                        .apply {
                            publicKey.extensions?.appId?.let { appId ->
                                setFido2Extension(FidoAppIdExtension(appId))
                            }
                            publicKey.extensions?.thirdPartyPayment?.let { thirdPartyPayment ->
                                setGoogleThirdPartyPaymentExtension(
                                    GoogleThirdPartyPaymentExtension(thirdPartyPayment)
                                )
                            }
                            publicKey.extensions?.uvm?.let { uvm ->
                                setUserVerificationMethodExtension(UserVerificationMethodExtension(uvm))
                            }
                        }
                        .build()
                    )
                }
            }
            .setChallenge(publicKey.challenge.toByteArray())
            .setRpId(rpId)
            .setTimeoutSeconds(publicKey.timeout?.toDouble()?.milliseconds?.inWholeSeconds?.toDouble())
            .build()
        // Note: `publicKey.userVerification` is currently not supported by the Fido client.

        return suspendCancellableCoroutine { continuation ->
            fidoClient.getSignPendingIntent(options).addOnSuccessListener { pendingIntent ->
                val senderRequest = IntentSenderRequest.Builder(pendingIntent).build()
                launcher.launch(senderRequest)
                continuation.resume(PerformTwoFaWithSecurityKey.LaunchResult.Success)
            }.addOnCanceledListener {
                continuation.cancel()
            }.addOnFailureListener {
                continuation.resume(PerformTwoFaWithSecurityKey.LaunchResult.Failure(it))
            }
        }
    }

    override fun register(
        activity: ComponentActivity,
        onResult: (PerformTwoFaWithSecurityKey.Result, Fido2PublicKeyCredentialRequestOptions) -> Unit
    ) {
        launcher =
            activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                val bytes = activityResult.data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
                val result = if (activityResult.resultCode != Activity.RESULT_OK) {
                    PerformTwoFaWithSecurityKey.Result.Cancelled
                } else if (bytes == null) {
                    PerformTwoFaWithSecurityKey.Result.EmptyResult
                } else {
                    val credential = PublicKeyCredential.deserializeFromBytes(bytes)
                    handleCredentialResponse(credential)
                }

                val options = requireNotNull(publicKeyOptions)
                onResult(result, options)
            }
    }

    private fun handleCredentialResponse(credential: PublicKeyCredential): PerformTwoFaWithSecurityKey.Result =
        when (val response = credential.response) {
            is AuthenticatorErrorResponse -> PerformTwoFaWithSecurityKey.Result.Error(
                PerformTwoFaWithSecurityKey.ErrorData(response.errorCode.convert(), response.errorMessage)
            )

            is AuthenticatorAssertionResponse -> PerformTwoFaWithSecurityKey.Result.Success(
                rawId = credential.rawId!!,
                authenticatorAttachment = credential.authenticatorAttachment!!,
                type = credential.type,
                id = credential.id!!,
                response = PerformTwoFaWithSecurityKey.SuccessResponseData(
                    clientDataJSON = response.clientDataJSON,
                    authenticatorData = response.authenticatorData,
                    signature = response.signature
                ),
                // Note: `credential.clientExtensionResults` are currently not passed down.
            )

            else -> PerformTwoFaWithSecurityKey.Result.UnknownResult
        }
}

/**
 * We need to convert [ErrorCode] to our own type,
 * in case the FIDO2 library is not included (e.g. on builds for F-Droid).
 */
private fun ErrorCode.convert(): PerformTwoFaWithSecurityKey.ErrorCode = when (this) {
    ErrorCode.NOT_SUPPORTED_ERR -> ProtonErrorCode.NOT_SUPPORTED_ERR
    ErrorCode.INVALID_STATE_ERR -> ProtonErrorCode.INVALID_STATE_ERR
    ErrorCode.SECURITY_ERR -> ProtonErrorCode.SECURITY_ERR
    ErrorCode.NETWORK_ERR -> ProtonErrorCode.NETWORK_ERR
    ErrorCode.ABORT_ERR -> ProtonErrorCode.ABORT_ERR
    ErrorCode.TIMEOUT_ERR -> ProtonErrorCode.TIMEOUT_ERR
    ErrorCode.ENCODING_ERR -> ProtonErrorCode.ENCODING_ERR
    ErrorCode.UNKNOWN_ERR -> ProtonErrorCode.UNKNOWN_ERR
    ErrorCode.CONSTRAINT_ERR -> ProtonErrorCode.CONSTRAINT_ERR
    ErrorCode.DATA_ERR -> ProtonErrorCode.DATA_ERR
    ErrorCode.NOT_ALLOWED_ERR -> ProtonErrorCode.NOT_ALLOWED_ERR
    ErrorCode.ATTESTATION_NOT_PRIVATE_ERR -> ProtonErrorCode.ATTESTATION_NOT_PRIVATE_ERR
}
