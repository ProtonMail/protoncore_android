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

package me.proton.core.configuration

import android.content.Context
import android.database.Cursor
import android.net.Uri
import me.proton.core.configuration.extension.contentValues

public open class ContentResolverConfigManager(
    private val context: Context
) {
    @Synchronized
    public fun fetchConfigDataFromContentResolver(): Map<String, Any?>? = context.contentResolver.query(
        CONFIG_CONTENT_URI,
        null,
        null,
        null,
        null
    )?.use { cursor ->
        cursor.columnNames.associateWith { columnName ->
            cursor.retrieveValue(columnName)
        }
    }

    @Synchronized
    public fun insertConfiguration(configuration: EnvironmentConfiguration): Uri? = context.contentResolver.insert(
        CONFIG_CONTENT_URI,
        configuration.contentValues
    )

    private fun Cursor.retrieveValue(columnName: String): Any? {
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex == -1) return null
        return if (moveToFirst()) getString(columnIndex) else null
    }

    private companion object {
        private const val CONFIG_AUTHORITY = "me.proton.core.configuration.configurator"
        val CONFIG_CONTENT_URI: Uri = Uri.parse("content://$CONFIG_AUTHORITY/config")
    }
}
