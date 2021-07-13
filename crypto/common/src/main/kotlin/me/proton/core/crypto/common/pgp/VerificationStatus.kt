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

package me.proton.core.crypto.common.pgp

/**
 * Signature verification status.
 *
 * @see [PGPCrypto.decryptAndVerifyText]
 * @see [PGPCrypto.decryptAndVerifyData]
 */
enum class VerificationStatus {
    /** No verification at all. */
    Unknown,

    /** Embedded signature is correct. */
    Success,

    /** Embedded signature doesn't exist. */
    NotSigned,

    /** Embedded signature doesn't match the provided publicKey to verify. */
    NotMatchKey,

    /** Embedded signature match the provided publicKey but is incorrect. */
    Failure,
}
