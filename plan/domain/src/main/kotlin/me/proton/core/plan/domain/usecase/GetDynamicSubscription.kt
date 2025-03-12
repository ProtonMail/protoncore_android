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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.canReadSubscription
import me.proton.core.util.kotlin.runCatchingCheckedExceptions
import javax.inject.Inject

/**
 * Gets current active dynamic subscription a user has.
 * Authorized. This means that it could only be used for upgrades. New accounts created during sign ups logically do not
 * have existing subscriptions.
 */
public class GetDynamicSubscription @Inject constructor(
    private val plansRepository: PlansRepository,
    private val userManager: UserManager
) {
    public suspend operator fun invoke(userId: UserId): DynamicSubscription? = runCatchingCheckedExceptions {
        when {
            userManager.getUser(userId).canReadSubscription() -> plansRepository.getDynamicSubscriptions(userId).first()
            else -> null
        }
    }.getOrNull()
}
