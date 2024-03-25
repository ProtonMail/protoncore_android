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

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

public open class ContentResolverConfigManager(
    public val context: Context
) {
    @Synchronized
    public fun fetchConfigurationDataAtPath(path: String): Map<String, Any?>? = context.contentResolver.query(
        path.contentResolverUrl,
        null,
        null,
        null,
        null
    )?.use { cursor ->
        cursor.columnNames.associateWith { columnName ->
            cursor.retrieveValue(columnName)
        }
    }?.takeIf {
        it.isNotEmpty()
    }

    @Synchronized
    public fun insertContentValuesAtPath(configFieldMap: Map<String, Any?>, path: String): Uri? =
        context.contentResolver.insert(path.contentResolverUrl, contentValues(configFieldMap))

    private val String.contentResolverUrl: Uri get() = Uri.parse("content://$CONFIG_AUTHORITY/config/$this")

    private fun Cursor.retrieveValue(columnName: String): Any? {
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex == -1) return null
        return if (moveToFirst()) getString(columnIndex) else null
    }

    private fun contentValues(map: Map<String, Any?>): ContentValues = ContentValues().apply {
        map.forEach { (key, value) ->
            when (value) {
                is String -> put(key, value)
                is Boolean -> put(key, value)
            }
        }
    }

    public companion object {
        private const val CONFIG_AUTHORITY = "me.proton.core.configuration.configurator"
    }
}
