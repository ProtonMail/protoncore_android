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

package me.proton.core.auth.presentation.compose.sso

import me.proton.core.auth.domain.entity.DeviceSecretString

public sealed class MemberApprovalState(
    public open val email: String? = null
) {

    public data class Idle(
        override val email: String? = null
    ) : MemberApprovalState()

    public data class Loading(
        override val email: String? = null
    ) : MemberApprovalState()

    public data class Confirming(
        override val email: String? = null
    ) : MemberApprovalState()

    public data class Rejecting(
        override val email: String? = null
    ) : MemberApprovalState()

    public data class Valid(
        override val email: String? = null,
        val deviceSecret: DeviceSecretString
    ) : MemberApprovalState(email)

    public data class Error(
        override val email: String? = null,
        val message: String? = null
    ) : MemberApprovalState()

    // Terminal states:
    public data object Closed : MemberApprovalState()
    public data object Confirmed : MemberApprovalState()
    public data object Rejected : MemberApprovalState()
}
