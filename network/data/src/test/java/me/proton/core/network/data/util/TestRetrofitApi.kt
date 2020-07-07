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
package me.proton.core.network.data.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.IntToBoolSerializer
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Tag

@Serializable
class TestResult(
    @SerialName("Number") val number: Int,
    @SerialName("String") val string: String,

    @Serializable(with = IntToBoolSerializer::class)
    @SerialName("Bool") val bool: Boolean = true
)

interface TestExtensionRetrofitApi {

    @GET("test")
    suspend fun test(): TestResult

    @GET("testPlain")
    @Headers("Accept: text/plain")
    suspend fun testPlain(): String

    @GET("testLong")
    suspend fun testLongTimeouts(@Tag tag: Any?): String
}

interface TestRetrofitApi : BaseRetrofitApi,
    TestExtensionRetrofitApi
