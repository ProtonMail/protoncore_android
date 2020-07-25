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

package me.proton.core.humanverification.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.util.kotlin.Invokable

/**
 * Local repository interface that defines all operations that later the data layer (module) should
 * implement. All of these operations are local and read from local resources. Anyway, run them on
 * an IO thread.
 *
 * @author Dino Kadrikj.
 */
interface HumanVerificationLocalRepository : Invokable {

    /**
     * Returns all [Country] list.
     */
    fun allCountries(mostUsedIncluded: Boolean): Flow<List<Country>>

    /**
     * Returns the most used [Country] list of countries.
     */
    fun mostUsedCountries(): Flow<List<Country>>
}
