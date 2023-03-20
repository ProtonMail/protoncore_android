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

package me.proton.core.crypto.common.pgp


/**
 * [SignatureContext] is a special value specified
 * by the signer to provide context separation of detached signatures.
 *
 * Example: If a client creates a signature for an email attachment (with the context set to "mail-attachment"),
 * this signature can't be maliciously used to pretend the attachment is a drive key passphrase
 * (if the drive client expects the context "drive-key-passphrase").
 *
 * @param value: the context value will depend on what is being signed.
 * This must be a documented constant on which all clients agree.
 *
 * @param isCritical: If set to true, the verification will fail for all clients
 * that verify without expecting a context to be set. It must only be set to true once
 * all clients (and server) that are expected to verify the signature have been upgraded to
 * expect the context.
 *
 * See [VerificationContext].
 */
data class SignatureContext(
    val value: String,
    val isCritical: Boolean = false,
)

/**
 * [VerificationContext] is used to enforce that a signature was created in the expected context.
 *
 * @param value the context value will depend on what is being verified.
 * This must be a documented constant on which all clients agree.
 *
 * @param required Specifies if the context is required, to allow for backward compatibility
 * with signatures created before contexts were added to signatures.
 * - [ContextRequirement.NotRequired] the signature is allowed to have no
 * context, but not a different context than the one provided. This should be use as a first
 * phase for existing signatures, while we upgrade all signing clients to add the context.
 * - [ContextRequirement.Required.After] the signature is allowed to have no
 * context only if it was generated before [ContextRequirement.Required.After.timestamp].
 * This should be used as a second phase for existing signatures,
 * once all signing clients have been upgraded.
 * - [ContextRequirement.Required.Always] the signature is always expected to have the context.
 * This can be used for contexts where we don't need backward compatibility with existing signatures.
 * Example: new feature, new product.
 *
 *
 * See [SignatureContext].
 */
data class VerificationContext(
    val value: String,
    val required: ContextRequirement
){
    sealed class ContextRequirement{
        object NotRequired: ContextRequirement()
        sealed class Required: ContextRequirement() {
            object Always: Required()
            data class After(val timestamp: Long): Required()
        }
    }
}