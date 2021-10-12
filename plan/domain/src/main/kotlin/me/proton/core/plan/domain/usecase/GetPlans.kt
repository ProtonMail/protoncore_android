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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Inject

class GetPlans @Inject constructor(
    private val plansRepository: PlansRepository
) {
    suspend operator fun invoke(supportedPaidPlans: List<String>, userId: UserId?): List<Plan> {
        return plansRepository.getPlans(userId).filter {
            supportedPaidPlans.contains(it.name)
        }
    }
}
