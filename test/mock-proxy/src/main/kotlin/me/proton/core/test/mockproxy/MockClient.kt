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

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

public class MockClient(private val baseUrl: String, private val proxyToken: String = "") {

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                        .newBuilder()
                        .addHeader("x-atlas-secret", proxyToken)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )

    private var mockApi: MockApi = retrofitBuilder
        .addConverterFactory(Json.asConverterFactory(MimeType.JSON.value.toMediaType()))
        .build()
        .create(MockApi::class.java)

    public fun setSRPMock(
        shouldEnable: Boolean = false,
        password: String
    ) {
        Log.d(
            "MockProxy",
            "Executing SRP request with baseUrl: ${this.baseUrl} and proxyToken: ${this.proxyToken}"
        )
        val srpMockList = DynamicMockObject(
            name = "loginWithSrp",
            enabled = shouldEnable,
            parameters = mapOf("password" to password)
        )
        runBlocking { mockApi.setDynamicMocks(srpMockList) }
    }

    public fun setRecording(
        shouldEnable: Boolean = false,
        recordingDirPath: String
    ) {
        Log.d(
            "MockProxy",
            "Executing set recording request with baseUrl: ${this.baseUrl} and proxyToken: ${this.proxyToken}"
        )
        val mock = DynamicMockObject(
            name = "recordAll",
            enabled = shouldEnable,
            updateFile = true,
            parameters = mapOf("mockDirectory" to recordingDirPath)
        )
        runBlocking {
            mockApi.setDynamicMocks(mock)
        }
    }

    public fun setScenarioFromAssets(
        scenarioFilePath: String,
        shouldUpdateFile: Boolean = false
    ): List<MockObject> {
        val staticMocksList =
            MockParser.parseScenarioFileFromAssets(scenarioFilePath, true, shouldUpdateFile)
        return this.setStaticMocks(staticMocksList)
    }

    public fun resetScenarioFromAssets(scenarioFilePath: String): List<MockObject> {
        val staticMocksList = MockParser.parseScenarioFileFromAssets(scenarioFilePath, false)
        lateinit var mockObjectResponse: List<MockObject>
        runBlocking { mockObjectResponse = mockApi.setStaticMocks(staticMocksList) }
        return mockObjectResponse
    }

    public fun getScenarioDirPath(scenarioFilePath: String): String =
        MockParser.getScenarioDirectoryOrThrow(scenarioFilePath)

    public fun getStaticMocks(): List<MockObject> {
        lateinit var mockObjectResponse: List<MockObject>
        runBlocking { mockObjectResponse = mockApi.getStaticMocks() }
        return mockObjectResponse
    }

    public fun setStaticMockFromAssets(filePath: String): MockObject {
        val staticMockFile = MockParser.parseMockFileFromAssets(filePath)
        lateinit var mockObjectResponse: MockObject
        runBlocking { mockObjectResponse = mockApi.setStaticMock(staticMockFile) }
        return mockObjectResponse
    }

    private fun setStaticMocks(mockRoutesList: List<MockObject>): List<MockObject> {
        lateinit var mockObjectResponse: List<MockObject>
        runBlocking { mockObjectResponse = mockApi.setStaticMocks(mockRoutesList) }
        return mockObjectResponse
    }

    public fun setStaticMock(staticMock: MockObject): MockObject {
        lateinit var mockObjectResponse: MockObject
        runBlocking { mockObjectResponse = mockApi.setStaticMock(staticMock) }
        return mockObjectResponse
    }

    public fun resetStaticMock(staticMock: MockObject): MockObject {
        staticMock.enabled = false
        lateinit var mockObjectResponse: MockObject
        runBlocking { mockObjectResponse = mockApi.setStaticMock(staticMock) }
        return mockObjectResponse
    }

    public fun getLatency(): LatencyObject {
        lateinit var mockObjectResponse: LatencyObject
        runBlocking { mockObjectResponse = mockApi.getLatency() }
        return mockObjectResponse
    }

    public fun setLatency(latencyLevel: LatencyLevel): LatencyObject {
        val latencyInfo = LatencyObject(
            enabled = true,
            latency = latencyLevel.latencyMs
        )
        lateinit var mockObjectResponse: LatencyObject
        runBlocking { mockObjectResponse = mockApi.setLatency(latencyInfo) }
        return mockObjectResponse
    }

    public fun resetLatency(): LatencyObject {
        val latencyInfo = LatencyObject(
            enabled = false,
            latency = LatencyLevel.NONE.latencyMs
        )
        lateinit var mockObjectResponse: LatencyObject
        runBlocking { mockObjectResponse = mockApi.setLatency(latencyInfo) }
        return mockObjectResponse
    }

    public fun getBandwidth(): BandwidthObject {
        lateinit var mockObjectResponse: BandwidthObject
        runBlocking { mockObjectResponse = mockApi.getBandwidth() }
        return mockObjectResponse
    }

    public fun setBandwidth(bandwidthLimit: BandwidthLimit = BandwidthLimit.NONE): BandwidthObject {
        val bandwidthInfo = BandwidthObject(
            enabled = true,
            limit = bandwidthLimit.speedKbps
        )
        lateinit var mockObjectResponse: BandwidthObject
        runBlocking { mockObjectResponse = mockApi.setBandwidth(bandwidthInfo) }
        return mockObjectResponse
    }

    public fun resetBandwidth(): BandwidthObject {
        val bandwidthInfo = BandwidthObject(
            enabled = false,
            limit = BandwidthLimit.NONE.speedKbps
        )
        lateinit var mockObjectResponse: BandwidthObject
        runBlocking { mockObjectResponse = mockApi.setBandwidth(bandwidthInfo) }
        return mockObjectResponse
    }

    public fun resetAllMocks(): String {
        lateinit var mockObjectResponse: String
        runBlocking { mockObjectResponse = mockApi.resetAllMocks().message }
        return mockObjectResponse
    }
}
