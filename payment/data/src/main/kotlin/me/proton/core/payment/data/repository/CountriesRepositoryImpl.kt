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

package me.proton.core.payment.data.repository

import android.content.Context
import me.proton.core.data.assets.readFromAssets
import me.proton.core.payment.data.entity.CountriesDataModel
import me.proton.core.payment.domain.entity.Country
import me.proton.core.payment.domain.repository.CountriesRepository
import me.proton.core.util.kotlin.deserialize

class CountriesRepositoryImpl(private val context: Context) : CountriesRepository {

    override fun getCountries(): List<Country> {
        val fileContent = context.readFromAssets(FILE_NAME_ALL_COUNTRIES)
        val countries = fileContent.deserialize(CountriesDataModel.serializer())
        return countries.countries
            .filter { it.name.isNotEmpty() }
            .sortedBy { it.name }
            .map { Country(it.name, it.code) }
    }

    override fun getCountryCodeByName(countryName: String): String =
        getCountries().find {
            it.name == countryName
        }?.code ?: run {
            countryName
        }

    companion object {
        private const val FILE_NAME_ALL_COUNTRIES = "country_codes.json"
    }
}
