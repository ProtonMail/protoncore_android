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

package me.proton.core.test.mockproxy

import retrofit2.http.*

internal interface MockApi {

    /** Single route mock **/
    @POST("/mock/route/static")
    suspend fun setStaticMock(@Body mocks: MockObject): MockObject

    /** List of routes to mock **/
    @GET("/mock/routes/static")
    suspend fun getStaticMocks(): List<MockObject>
    @POST("/mock/routes/static")
    suspend fun setStaticMocks(@Body mocks: List<MockObject>): List<MockObject>

    /** List of routes to mock dynamically **/
    @GET("/mock/routes/dynamic")
    suspend fun getDynamicMocks(): List<DynamicMockObject>
    @POST("/mock/routes/dynamic")
    suspend fun setDynamicMocks(@Body mocks: DynamicMockObject): DynamicMockObject

    /** Latency endpoints **/
    @GET("/mock/latency")
    suspend fun getLatency(): LatencyObject
    @POST("/mock/latency")
    suspend fun setLatency(@Body latencyInfo: LatencyObject): LatencyObject

    /** Bandwidth endpoints **/
    @GET("/mock/bandwidth")
    suspend fun getBandwidth(): BandwidthObject
    @POST("/mock/bandwidth")
    suspend fun setBandwidth(@Body bandwidthInfo: BandwidthObject): BandwidthObject

    /** Reset endpoints **/
    @POST("/mock/reset/all")
    suspend fun resetAllMocks(): ResponseMessage
}
