/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.domain.usecase.sso

import me.proton.core.domain.entity.UserId
import javax.inject.Inject

/**
 * Sets an inactive device as rejected, for a member user.
 * Requires ORGANIZATION scope.
 * For rejecting a device for the currently logged in user, use [RejectAuthDevice].
 */
class RejectMemberDevice @Inject constructor() {
    /**
     * @param userId The ID of the admin user (the one currently logged in).
     * @param memberId The ID of the user to reject the device for.
     * @param deviceId The ID of the device to reject.
     */
    suspend operator fun invoke(
        userId: UserId,
        memberId: UserId,
        // deviceId: MemberDeviceId // TODO uncomment when MemberDeviceId class is merged
    ) {
        TODO()
    }
}
