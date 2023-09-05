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

import android.content.Context
import android.database.Cursor
import android.net.Uri
import me.proton.core.configuration.entity.ConfigFieldProvider

public open class ContentResolverConfigProvider(
    context: Context,
    private val defaultConfigProvider: ConfigFieldProvider = StaticConfigFieldProvider()
) : ConfigFieldProvider {

    public override val configData: Map<String, Any?> by lazy {
        fetchConfigDataFromContentResolver(context) ?: defaultConfigProvider.configData
    }

    private fun fetchConfigDataFromContentResolver(context: Context): Map<String, Any?>? =
        context.contentResolver.query(CONTENT_URI, null, null, null, null)?.use { cursor ->
            cursor.columnNames.associateWith { columnName ->
                cursor.retrieveValue(columnName)
            }
        }

    private fun Cursor.retrieveValue(columnName: String): Any? {
        val columnIndex = getColumnIndex(columnName)
        return if (columnIndex != -1 && moveToFirst()) getString(columnIndex) else null
    }

    private companion object {
        val CONTENT_URI: Uri = Uri.parse("content://me.proton.android.configurator/config")
    }
}
