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

package me.proton.core.crypto.android.pgp

import com.proton.gopenpgp.constants.Constants
import com.proton.gopenpgp.crypto.SignatureVerificationError
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException

fun SignatureVerificationError?.toVerificationStatus(): VerificationStatus = when {
    // No error -> success.
    this == null -> VerificationStatus.Success
    else -> status.toVerificationStatus()
}

fun Long.toVerificationStatus(): VerificationStatus = when (this) {
    Constants.SIGNATURE_OK -> VerificationStatus.Success
    Constants.SIGNATURE_NOT_SIGNED -> VerificationStatus.NotSigned
    Constants.SIGNATURE_NO_VERIFIER -> VerificationStatus.NotMatchKey
    Constants.SIGNATURE_FAILED -> VerificationStatus.Failure
    Constants.SIGNATURE_BAD_CONTEXT -> VerificationStatus.BadContext
    else -> throw CryptoException("Unknown SignatureVerificationError status: $this")
}
