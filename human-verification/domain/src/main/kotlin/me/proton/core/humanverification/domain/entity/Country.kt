package me.proton.core.humanverification.domain.entity

import kotlinx.serialization.Serializable

/**
 * Created by dinokadrikj on 6/18/20.
 */
@Serializable
data class Country(
    val code: String, // Country code
    val name: String,
    val callingCode: Int
)
