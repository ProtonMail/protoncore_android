package me.proton.core.util.kotlin

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * Configuration for Proton Core library
 * @author Davide Farella
 */
object ProtonCoreConfig : Invokable {

    /** Default [StringFormat] for serialize and deserialize JSON strings */
    var defaultJsonStringFormat = Json(JsonConfiguration.Stable.copy(
        ignoreUnknownKeys = true, isLenient = true))
}
