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

package me.proton.core.configuration.extension

import android.content.ContentValues
import me.proton.core.configuration.EnvironmentConfiguration
import java.lang.reflect.Field

public val EnvironmentConfiguration.configContractFields: Map<String, Field>
    get() = this::class.java.declaredFields.associateBy {
        it.isAccessible = true
        it.name
    }

public val EnvironmentConfiguration.configContractFieldsMap: Map<String, Any?>
    get() = configContractFields.mapValues {
        it.value.get(this)
    }

public val EnvironmentConfiguration.contentValues: ContentValues
    get() = ContentValues().also { contentValues ->
        configContractFields.forEach {
            val stringValue = it.value.get(this)?.toString()
            when (it.value.type) {
                String::class.java -> contentValues.put(it.key, stringValue)
                Boolean::class.java -> contentValues.put(it.key, stringValue.toBoolean())
            }
        }
    }