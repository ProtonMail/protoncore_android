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

package me.proton.core.countries.data.repository

import android.content.Context
import me.proton.core.countries.data.entity.CountriesDataModel
import me.proton.core.countries.data.entity.CountryDataModel
import me.proton.core.countries.domain.entity.Country
import me.proton.core.countries.domain.repository.CountriesRepository
import me.proton.core.data.asset.readFromAssets
import me.proton.core.util.kotlin.deserialize

const val FILE_NAME_ALL_COUNTRIES = "country_codes.json"

class CountriesRepositoryImpl(private val context: Context) : CountriesRepository {

    override suspend fun getAllCountriesSorted() = readCountriesFile()
        .filter { it.name.isNotEmpty() }
        .sortedBy { it.name }
        .map {
            Country(code = it.code, name = it.name, callingCode = it.callingCode)
        }

    override suspend fun getCountryCodeByName(countryName: String): String =
        getAllCountriesSorted().find {
            it.name == countryName
        }?.code ?: run {
            countryName
        }

    private fun readCountriesFile(): List<CountryDataModel> {
        val fileContent = context.readFromAssets(FILE_NAME_ALL_COUNTRIES)
        val json = fileContent.deserialize(CountriesDataModel.serializer())
        return json.countries
    }

    companion object {
        private const val FILE_NAME_ALL_COUNTRIES = "country_codes.json"
    }
}
