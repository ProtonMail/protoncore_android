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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass
import androidx.core.net.toUri

public class ContentResolverConfigManager @Inject constructor(
    @ApplicationContext public val context: Context
) {
    private val String.contentResolverUrl: Uri get() = "content://$CONFIG_AUTHORITY/config/$this".toUri()

    @Synchronized
    public fun queryAtClassPath(clazz: KClass<*>): Map<String, Any?>? {
        val cursor = context.contentResolver.query(
            clazz.qualifiedName?.contentResolverUrl ?: return null,
            null,
            null,
            null,
            null
        )

        return cursor?.use {
            cursor.columnNames.associateWith { columnName ->
                cursor.retrieveValue(columnName)
            }
        }?.takeIf {
            it.isNotEmpty()
        }
    }

    @Synchronized
    public fun insertConfigFieldMapAtClassPath(configFieldMap: Map<String, Any?>, clazz: KClass<*>): Uri? =
        context.contentResolver.insert(clazz.qualifiedName!!.contentResolverUrl, configFieldMap.contentValues)

    private val Map<String, Any?>.contentValues: ContentValues get() = ContentValues().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> put(key, value)
                is Boolean -> put(key, value)
                is Int -> put(key, value)
            }
        }
    }

    // Note - here any value is converted to string. Use it with this information in mind.
    private fun Cursor.retrieveValue(columnName: String): Any? {
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex == -1) return null
        return if (moveToFirst()) getString(columnIndex) else null
    }

    public companion object {
        internal const val CONFIG_AUTHORITY: String = "me.proton.core.configuration.configurator"
    }
}
