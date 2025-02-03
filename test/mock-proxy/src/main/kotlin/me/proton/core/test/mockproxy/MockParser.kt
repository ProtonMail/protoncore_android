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

import android.content.res.AssetManager
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

internal object MockParser {
    private val testContext
        get() = InstrumentationRegistry.getInstrumentation().context
    private val testAssetManager
        get() = testContext.assets ?: error("Could not load app assets.")

    private fun readFileFromAssets(filePath: String, assetManager: AssetManager): String =
        assetManager.open(filePath).bufferedReader().use { it.readText() }

    internal fun parseMockFileFromAssets(mockFilePath: String): MockObject {
        val assetManager = getAssetManager(mockFilePath)
        return parseAssetFileToStaticMock(mockFilePath, assetManager)
    }

    private fun parseAssetFileToStaticMock(
        mockFilePath: String,
        assetManager: AssetManager,
        isEnabled: Boolean = false
    ): MockObject {
        val mockFile = File(mockFilePath)
        val mockFileContent = readFileFromAssets(mockFilePath, assetManager)
        val decodedMockFile: MockFile = Json.decodeFromString(mockFileContent)
        val fileName = mockFile.nameWithoutExtension
        return MockObject(
            name = fileName,
            enabled = isEnabled,
            updateFile = false,
            request = decodedMockFile.request,
            response = decodedMockFile.response
        )
    }

    internal fun getScenarioDirectoryOrThrow(
        scenarioFilePath: String
    ): String {
        val assetManager = getAssetManager(scenarioFilePath)
        val fileContent = readFileFromAssets(scenarioFilePath, assetManager)
        val scenarioFileObject: ScenarioFileObject = Json.decodeFromString(fileContent)
        // Get the first mock file and check if it is a folder
        val firstMockFile = scenarioFileObject.mockFiles[0]
        val assetList = assetManager.list(firstMockFile)

        if (assetList.isNullOrEmpty()) {
            throw IllegalStateException("The first mock file is not a folder: $firstMockFile")
        }
        return firstMockFile
    }

    internal fun parseScenarioFileFromAssets(
        scenarioFilePath: String,
        isEnabled: Boolean,
        shouldUpdateFile: Boolean = false
    ): List<MockObject> {
        val assetManager = getAssetManager(scenarioFilePath)
        return parseAssetFileToListOfStaticMocks(
            scenarioFilePath,
            assetManager,
            isEnabled,
            shouldUpdateFile
        )
    }

    private fun getAssetManager(scenarioFilePath: String): AssetManager {
        val assetManager = runCatching {
            readFileFromAssets(
                scenarioFilePath,
                testAssetManager
            ).also { println("Read from testAssetManager") }
            testAssetManager
        }.getOrElse {
            throw FileNotFoundException(
                "Failed to read file from both test and app assets. File path: $scenarioFilePath"
            )
        }
        return assetManager
    }

    private fun parseAssetFileToListOfStaticMocks(
        scenarioFilePath: String,
        assetManager: AssetManager,
        isEnabled: Boolean = false,
        shouldUpdateFile: Boolean = false
    ): List<MockObject> {
        val mockRoutesList = mutableListOf<MockObject>()
        val fileContent = readFileFromAssets(scenarioFilePath, assetManager)
        val scenarioFileObject: ScenarioFileObject = Json.decodeFromString(fileContent)
        processMockFiles(
            scenarioFileObject.mockFiles,
            assetManager,
            isEnabled,
            shouldUpdateFile,
            mockRoutesList
        )
        return mockRoutesList
    }

    private fun processMockFiles(
        mockFiles: List<String>,
        assetManager: AssetManager,
        isEnabled: Boolean,
        shouldUpdateFile: Boolean,
        mockRoutesList: MutableList<MockObject>
    ) {
        mockFiles.forEach { mockFilePath ->
            val assetList = assetManager.list(mockFilePath)
            if (assetList.isNullOrEmpty()) {
                val mockFile = File(mockFilePath)
                val mockName =
                    "${System.currentTimeMillis()}-${mockFile.parentFile?.name}-${mockFile.nameWithoutExtension}"
                val mockFileContent = readFileFromAssets(mockFilePath, assetManager)
                val decodedMockFile: MockFile = Json.decodeFromString(mockFileContent)

                mockRoutesList.add(
                    MockObject(
                        name = mockName,
                        enabled = isEnabled,
                        updateFile = shouldUpdateFile,
                        request = decodedMockFile.request,
                        response = decodedMockFile.response
                    )
                )
            } else {
                assetList.forEach { nestedFileName ->
                    val nestedFilePath = "$mockFilePath/$nestedFileName"
                    processMockFiles(
                        listOf(nestedFilePath),
                        assetManager,
                        isEnabled,
                        shouldUpdateFile,
                        mockRoutesList
                    )
                }
            }
        }
    }
}
