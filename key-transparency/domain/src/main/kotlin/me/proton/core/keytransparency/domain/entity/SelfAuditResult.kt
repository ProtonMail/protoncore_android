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

package me.proton.core.keytransparency.domain.entity

import me.proton.core.user.domain.entity.UserAddress

/**
 * The result of a self audit
 * @param timestamp: the server time when the self audit was run
 * @param contactAudits: the results of auditing external contacts changes and recent address changes
 * @param selfAddressAudits: the results for auditing the user addresses.
 */
public sealed class SelfAuditResult(
    public val timestamp: Long
) {
    public class Success(
        timestamp: Long,
        public val contactAudits: Map<String, AddressChangeAuditResult>,
        public val selfAddressAudits: Map<UserAddress, UserAddressAuditResult>
    ) : SelfAuditResult(timestamp)

    public class Failure(timestamp: Long, public val cause: Throwable) : SelfAuditResult(timestamp)
}

public sealed class AddressChangeAuditResult {
    public object Success : AddressChangeAuditResult()
    public data class Failure(val cause: Throwable) : AddressChangeAuditResult()
}
