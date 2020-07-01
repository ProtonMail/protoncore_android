package ch.protonmail.libs.core

import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

/**
 * Configuration for Proton Core library
 * @author Davide Farella
 */
object ProtonCoreConfig : Invokable {

    /** Default [StringFormat] for serialize and deserialize JSON strings */
    var defaultJsonStringFormat: StringFormat = Json
}
