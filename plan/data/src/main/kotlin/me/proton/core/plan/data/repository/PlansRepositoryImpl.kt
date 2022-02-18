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

package me.proton.core.plan.data.repository

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.plan.data.api.PlansApi
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.repository.PlansRepository

class PlansRepositoryImpl(
    private val provider: ApiProvider
) : PlansRepository {

    /**
     * Returns from the API all plans available for the user in the moment.
     */
    override suspend fun getPlans(sessionUserId: SessionUserId?): List<Plan> =
        provider.get<PlansApi>(sessionUserId).invoke {
            getPlans().plans.map {
                it.toPlan()
            }
        }.valueOrThrow

    override suspend fun getPlansDefault(sessionUserId: SessionUserId?): Plan =
        provider.get<PlansApi>(sessionUserId).invoke {
            getPlansDefault().plan.toPlan()
        }.valueOrThrow
}
