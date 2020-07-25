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

package me.proton.core.humanverification.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.humanverification.data.entity.CountriesDataModel
import me.proton.core.humanverification.data.entity.CountryDataModel
import me.proton.core.humanverification.data.readFromAssets
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.util.kotlin.deserialize

/**
 * @author Dino Kadrikj.
 */
const val FILE_NAME_ALL_COUNTRIES = "country_codes.json"
const val FILE_NAME_MOST_USED_COUNTRIES = "country_codes_most_used.json"

class HumanVerificationLocalRepositoryImpl(private val context: Context) :
    HumanVerificationLocalRepository {

    /** Returns all countries. */
    override fun allCountries(mostUsedIncluded: Boolean): Flow<List<Country>> = flow {
        val otherCountries = readAllOtherCountries()
        val fileContent =
            if (mostUsedIncluded) context.readFromAssets(FILE_NAME_MOST_USED_COUNTRIES) else null
        fileContent?.let { content ->
            val json = content.deserialize(CountriesDataModel.serializer())
            val countries =
                if (!mostUsedIncluded) otherCountries else json.countries.plus(otherCountries)
            emit(countries.map {
                Country(it.code, it.name, it.callingCode)
            })
        } ?: run {
            emit(otherCountries.map {
                Country(it.code, it.name, it.callingCode)
            })
        }
    }

    /**
     * Should fetch the most used countries.
     */
    override fun mostUsedCountries(): Flow<List<Country>> = flow {
        val fileContent = context.readFromAssets(FILE_NAME_MOST_USED_COUNTRIES)
        val json = fileContent.deserialize(CountriesDataModel.serializer())
        emit(json.countries.map {
            Country(it.code, it.name, it.callingCode)
        })
    }

    private fun readAllOtherCountries(): List<CountryDataModel> {
        val fileContent = context.readFromAssets(FILE_NAME_ALL_COUNTRIES)
        val json = fileContent.deserialize(CountriesDataModel.serializer())
        return json.countries
    }
}
