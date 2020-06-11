package me.proton.core.humanverification.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import me.proton.core.humanverification.data.entity.CountryDataModel
import me.proton.core.humanverification.data.readFromAssets
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.humanverification.domain.repository.LocalRepository

/**
 * Created by dinokadrikj on 6/19/20.
 */
class LocalRepositoryImpl(private val context: Context) : LocalRepository {
    /**
     * Returns all countries.
     */
    override fun allCountries(): Flow<List<Country>> = flow {
        val fileContent = context.readFromAssets("country_codes.json")
        val json = Json.parse(CountryDataModel.serializer().list, fileContent!!)
        emit(json.map {
            Country(it.code, it.name, it.callingCode)
        })
    }
}
