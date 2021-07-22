/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.data.room.db

import androidx.room.TypeConverter
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

class CommonConverters {

    @TypeConverter
    fun fromListOfStringToString(value: List<String>?): String? = Companion.fromListOfStringToString(value)

    @TypeConverter
    fun fromStringToListOfString(value: String?): List<String>? = Companion.fromStringToListOfString(value)

    @TypeConverter
    fun fromProductToString(value: Product?): String? = value?.name

    @TypeConverter
    fun fromStringToProduct(value: String?): Product? = value?.let {
        Product.valueOf(value)
    }

    @TypeConverter
    fun fromUserIdToString(value: UserId?): String? = value?.id

    @TypeConverter
    fun fromStringToUserId(value: String?): UserId? = value?.let {
        UserId(value)
    }

    @TypeConverter
    fun fromSessionIdToString(value: SessionId?): String? = value?.id

    @TypeConverter
    fun fromStringToSessionId(value: String?): SessionId? = value?.let {
        SessionId(value)
    }

    companion object {
        fun fromListOfStringToString(value: List<String>?): String? = value?.joinToString(separator = ";")
        fun fromStringToListOfString(value: String?): List<String>? = value?.split(";")?.toList()
    }
}
