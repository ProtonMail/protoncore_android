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

package me.proton.android.core.data.api.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource

/**
 * A model representing a generic [ResponseBody] received during an failed API call
 * @author Davide Farella
 */
@Serializable data class ApiErrorResponseBody(

    @SerialName(Field.DETAILS)
    val details: Map<String, String> = emptyMap()

) : ResponseBody() {

    /**
     * Returns the number of bytes in that will returned by [.bytes], or [.byteStream], or
     * -1 if unknown.
     */
    override fun contentLength() = -1L

    override fun contentType(): MediaType? = null

    override fun source() : BufferedSource? = null
}
