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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.keytransparency.domain.KeyTransparencyLogger
import me.proton.core.keytransparency.domain.entity.AddressChangeAuditResult
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.entity.UserAddressAuditResult
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.UnverifiableSKLException
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

internal class LogKeyTransparency @Inject constructor() {

    fun logPublicAddressVerification(result: PublicKeyVerificationResult) {
        when (result) {
            is PublicKeyVerificationResult.Failure -> {
                KeyTransparencyLogger.e(result.cause, "Public address verification failed")
            }
            is PublicKeyVerificationResult.Success -> {
                when (result.state) {
                    is VerifiedState.Existent ->
                        KeyTransparencyLogger.d("Public address is correctly included in KT")
                    is VerifiedState.Absent ->
                        KeyTransparencyLogger.d("Public address is not included in KT")
                    is VerifiedState.Obsolete ->
                        KeyTransparencyLogger.d("Public address is obsolete in KT")
                    is VerifiedState.NotYetIncluded ->
                        KeyTransparencyLogger.d("Public address is not yet in KT")
                }.exhaustive
            }
        }
    }

    fun logSelfAuditResult(result: SelfAuditResult) {
        when (result) {
            is SelfAuditResult.Failure -> {
                KeyTransparencyLogger.e(result.cause, "Self audit failed")
            }
            is SelfAuditResult.Success -> {
                KeyTransparencyLogger.d("Self audit succeeded, timestamp = ${result.timestamp}")
                result.logUserAddressAudits()
                result.logPublicAddressAudits()
            }
        }
    }

    private fun SelfAuditResult.Success.logUserAddressAudits() {
        selfAddressAudits.forEach { (_, addressAudit) ->
            when (addressAudit) {
                is UserAddressAuditResult.Success ->
                    KeyTransparencyLogger.d("Address audit succeeded")
                is UserAddressAuditResult.Failure ->
                    KeyTransparencyLogger.e(addressAudit.reason, "Address audit failed")
                is UserAddressAuditResult.Warning.ObsolescenceWarning ->
                    KeyTransparencyLogger.d("Address is marked as obsolete")
                is UserAddressAuditResult.Warning.AddressNotInKT ->
                    KeyTransparencyLogger.d("Address is not in KT")
                is UserAddressAuditResult.Warning.CreationTooRecent ->
                    KeyTransparencyLogger.d("Can't create verified epoch")
                is UserAddressAuditResult.Warning.Disabled ->
                    KeyTransparencyLogger.d("Address is disabled")
            }.exhaustive
        }
    }

    private fun SelfAuditResult.Success.logPublicAddressAudits() {
        contactAudits.forEach { (_, result) ->
            when (result) {
                is AddressChangeAuditResult.Success -> {
                    KeyTransparencyLogger.d("Address change audit succeeded")
                }
                is AddressChangeAuditResult.Failure -> {
                    val reason = result.cause
                    if (reason is UnverifiableSKLException) {
                        // There are edge case where an old SKL can legitimately fail verification
                        KeyTransparencyLogger.e(
                            reason,
                            "Address change audit failed because of SKL verification (warning)"
                        )
                    } else {
                        KeyTransparencyLogger.e(reason, "Address change audit failed")
                    }
                }
            }
        }
    }
}
