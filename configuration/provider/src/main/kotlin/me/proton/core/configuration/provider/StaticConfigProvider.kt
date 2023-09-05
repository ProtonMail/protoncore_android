/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.configuration.provider

import me.proton.core.configuration.entity.ConfigFieldProvider
import me.proton.core.configuration.extension.testTag
import me.proton.core.util.kotlin.CoreLogger

public const val DEFAULTS_CLASS: String = "me.proton.core.configuration.EnvironmentConfigurationDefaults"

public class StaticConfigFieldProvider(className: String = DEFAULTS_CLASS) : ConfigFieldProvider {

    override val configData: Map<String, Any?>

    init {
        val classNotFoundErrorMessage =
            "Class not found: $className. Make sure environment configuration gradle plugin is enabled!"

        configData = try {
            val defaultsClass = Class.forName(className)
            val instance = defaultsClass.newInstance()

            defaultsClass
                .declaredFields
                .associate { property ->
                    property.isAccessible = true
                    val propertyValue = property.get(instance)

                    require(propertyValue.isValidConfigValue()) {
                        "Unexpected value type for property: ${property.name}. " +
                            "Expected String, Boolean, or null. Found ${propertyValue?.javaClass?.name}."
                    }

                    property.name to propertyValue
                }
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(classNotFoundErrorMessage, e)
        }
    }

    private fun Any?.isValidConfigValue() =
        this is String || this is Boolean || this == null
}
