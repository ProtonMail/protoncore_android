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

/**
 * The result of the self audit for a single address
 */
public sealed class UserAddressAuditResult {
    /**
     * The audit has succeeded
     */
    public object Success : UserAddressAuditResult()

    /**
     * Some scenarios where it's expected for the self audit to fail.
     */
    public sealed class Warning : UserAddressAuditResult() {

        /**
         * The address has an obsolescence token but was not disabled
         */
        public object ObsolescenceWarning : Warning()

        /**
         * The address was created too recently to create a verified epoch
         */
        public object CreationTooRecent : Warning()

        /**
         * The address has no signed key list (only created on web for existent addresses).
         */
        public object AddressNotInKT : Warning()

        /**
         * The address is disabled
         */
        public object Disabled : Warning()
    }

    /**
     * The self audit has failed.
     */
    public data class Failure(val reason: Throwable) : UserAddressAuditResult()
}
