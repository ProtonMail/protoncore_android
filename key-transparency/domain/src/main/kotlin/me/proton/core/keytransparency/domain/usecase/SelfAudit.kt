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

import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.entity.AddressChangeAuditResult
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.mapAsync
import javax.inject.Inject

internal class SelfAudit @Inject internal constructor(
    private val repository: KeyTransparencyRepository,
    private val verifyAddressChangeWasIncluded: VerifyAddressChangeWasIncluded,
    private val auditUserAddress: AuditUserAddress,
    private val getCurrentTime: GetCurrentTime
) {

    suspend operator fun invoke(userId: UserId, userAddresses: List<UserAddress>): SelfAuditResult = try {
        val verificationBlobs = repository.getAllAddressChanges(userId)
        val contactAudits = verificationBlobs.mapAsync { contactToVerify ->
            val result = try {
                verifyAddressChangeWasIncluded(userId, contactToVerify)
                AddressChangeAuditResult.Success
            } catch (exception: KeyTransparencyException) {
                AddressChangeAuditResult.Failure(exception)
            }
            contactToVerify.email to result
        }.toMap()
        val selfAddressAudits = userAddresses.mapAsync { userAddress ->
            userAddress to auditUserAddress(userId, userAddress)
        }.toMap()
        SelfAuditResult.Success(
            timestamp = getCurrentTime(),
            contactAudits = contactAudits,
            selfAddressAudits = selfAddressAudits
        )
    } catch (exception: KeyTransparencyException) {
        SelfAuditResult.Failure(timestamp = getCurrentTime(), cause = exception)
    }
}
