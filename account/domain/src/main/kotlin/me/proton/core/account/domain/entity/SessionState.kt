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

package me.proton.core.account.domain.entity

enum class SessionState {
    /**
     * A second factor is needed.
     *
     * Note: Usually followed by either [SecondFactorSuccess] or [SecondFactorFailed].
     *
     * @see [SecondFactorSuccess]
     * @see [SecondFactorFailed].
     */
    SecondFactorNeeded,

    /**
     * The second factor has been successful.
     *
     * Note: Usually followed by [Authenticated].
     */
    SecondFactorSuccess,

    /**
     * The second factor has failed.
     *
     * Note: Client should consider calling [startLoginWorkflow].
     */
    SecondFactorFailed,

    /**
     * This [Session] is fully authenticated, no additional step needed.
     */
    Authenticated,

    /**
     * This [Session] is no longer valid.
     *
     * Note: Client should consider calling [startLoginWorkflow].
     */
    ForceLogout
}
