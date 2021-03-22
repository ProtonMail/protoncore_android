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

package me.proton.core.countries.domain.repository

import me.proton.core.countries.domain.entity.Country

/**
 * Local repository interface that defines all operations that later the data layer (module) should
 * implement. All of these operations are local and read from local resources. Anyway, run them on
 * an IO thread.
 */
interface CountriesRepository {

    suspend fun getAllCountriesSorted(): List<Country>

    suspend fun getCountryCodeByName(countryName: String): String
}
