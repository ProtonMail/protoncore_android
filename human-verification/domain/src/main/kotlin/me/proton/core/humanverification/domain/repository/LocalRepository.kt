package me.proton.core.humanverification.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.humanverification.domain.entity.Country
import me.proton.core.util.kotlin.Invokable

/**
 * Created by dinokadrikj on 6/19/20.
 */
interface LocalRepository : Invokable {

    /**
     * Returns all countries.
     */
    fun allCountries(): Flow<List<Country>>
}
