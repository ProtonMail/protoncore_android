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

package me.proton.core.network.data

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A converter for returning the whole body as a ByteArray (allows for ApiResult<ByteArray>).
 */
class ByteArrayConverter : Converter<ResponseBody, ByteArray> {
    class Factory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation?>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *>? {
            if (getRawType(type) != ByteArray::class.java)
                return null

            return INSTANCE
        }
    }

    override fun convert(value: ResponseBody): ByteArray =
        value.use { it.bytes() }

    companion object {
        private val INSTANCE = ByteArrayConverter()
    }
}
