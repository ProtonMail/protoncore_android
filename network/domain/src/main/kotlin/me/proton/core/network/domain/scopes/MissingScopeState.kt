/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.network.domain.scopes

enum class MissingScopeState {

    /**
     * A password scope is needed.
     *
     * Note: Usually followed by either [ScopeObtainSuccess] or [ScopeObtainFailed].
     *
     * @see [ScopeObtainSuccess]
     * @see [ScopeObtainFailed].
     */
    PasswordScopeMissing,

    /**
     * A locked scope is needed.
     *
     * Note: Usually followed by either [ScopeObtainSuccess] or [ScopeObtainFailed].
     *
     * @see [ScopeObtainSuccess]
     * @see [ScopeObtainFailed].
     */
    LockedScopeMissing,

    /**
     * The missing scope has been obtained successfully.
     */
    ScopeObtainSuccess,

    /**
     * The user has not verified and the scope has not been granted.
     */
    ScopeObtainFailed;
}
