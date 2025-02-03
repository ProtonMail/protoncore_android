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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class MockObject(
    var name: String,
    var enabled: Boolean,
    var updateFile: Boolean? = null,
    val isRawFileContent: Boolean = false,
    var request: MockFile.RequestData,
    var response: MockFile.ResponseData
)

@Serializable
public data class DynamicMockObject(
    var name: String,
    var enabled: Boolean,
    var description: String? = null,
    var updateFile: Boolean? = null,
    var parameters: Map<String, String>? = null,
    var mocks: List<DynamicMock>? = null
)

@Serializable
public data class DynamicMock(
    var request: MockFile.RequestData? = null,
    var response: MockFile.ResponseData? = null
)

@Serializable
public data class ScenarioMockObject(
    val scenarioName: String,
    var enabled: Boolean,
    val updateFile: Boolean? = null
)

@Serializable
public data class LatencyObject(
    var enabled: Boolean,
    val latency: Int
)

@Serializable
public data class BandwidthObject(
    var enabled: Boolean,
    val limit: Int
)

@Serializable
public data class ScenarioFileObject(
    val description: String,
    val updateFile: Boolean,
    val mockFiles: List<String>
)

@Serializable
public data class MockFile(
    val request: RequestData,
    val response: ResponseData,
    val name: String,
    var enabled: Boolean,
    val meta: MetaData? = null
) {
    @Serializable
    public data class RequestData(
        val exactUrl: List<String>? = listOf(),
        val matchUrl: List<String>? = listOf(),
        val method: String? = null
    )

    @Serializable
    public data class ResponseData(
        val headers: Map<String, JsonElement>? = null,
        val body: JsonElement? = null,
        val statusCode: Int,
    )

    @Serializable
    public data class MetaData(
        val test: String,
        val description: String,
    )
}

@Serializable
public data class ParsedScenarioFile(
    val name: String,
    var enabled: Boolean,
    val request: MockFile.RequestData,
    val response: MockFile.ResponseData
)

@Serializable
public data class ResponseMessage(val message: String)
