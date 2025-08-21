/*
 * Copyright (c) 2024 Proton Technologies AG
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

import android.content.Context
import me.proton.core.configuration.entity.EnvironmentConfigFieldProvider
import me.proton.core.configuration.extension.primitiveFieldMap

public class StaticClassConfigFieldProvider(
    private val className: String
) : EnvironmentConfigFieldProvider {

    private val staticConfigDataMap =
        runCatching {
            val defaultsClass = Class.forName(className)
            val instance = defaultsClass.newInstance()
            instance.primitiveFieldMap
        }.onFailure {
            error("Class not found: $className!")
        }.getOrThrow()

    private val mapConfigFieldProvider = MapConfigFieldProvider(staticConfigDataMap)

    override fun getString(key: String): String? = mapConfigFieldProvider.getString(key)
    override fun getBoolean(key: String): Boolean? = mapConfigFieldProvider.getBoolean(key)
    override fun getInt(key: String): Int? = mapConfigFieldProvider.getInt(key)
}
