package me.proton.core.humanverification.data.entity

import kotlinx.serialization.Serializable

/**
 * Created by dinokadrikj on 6/18/20.
 */
@Serializable
data class CountryDataModel(
    val code: String,
    val name: String,
    val callingCode: Int
)
