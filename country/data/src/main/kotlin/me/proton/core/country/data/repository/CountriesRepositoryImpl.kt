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

package me.proton.core.country.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.country.data.entity.CountriesDataModel
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.repository.CountriesRepository
import me.proton.core.data.asset.readFromAssets
import me.proton.core.util.kotlin.deserialize

class CountriesRepositoryImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountriesRepository {

    override suspend fun getAllCountriesSorted() = readCountriesFile()
        .filter { it.name.isNotEmpty() }
        .sortedBy { it.name }
        .map {
            Country(code = it.code, name = it.name, callingCode = it.callingCode)
        }

    override suspend fun getCountry(countryName: String): Country? =
        getAllCountriesSorted().find {
            it.name == countryName
        }

    private suspend fun readCountriesFile() = withContext(dispatcher) {
        val fileContent = context.readFromAssets(FILE_NAME_ALL_COUNTRIES)
        fileContent.deserialize(CountriesDataModel.serializer()).countries
    }

    companion object {
        const val FILE_NAME_ALL_COUNTRIES = "country_codes.json"
    }
}
