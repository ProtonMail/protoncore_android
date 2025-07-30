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

package me.proton.core.test.rule

import android.app.UiAutomation
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.IOException

/**
 * A JUnit TestWatcher that:
 * - Records screen and logcat output during instrumentation tests.
 * - Saves output to Documents/test_artifacts/<TestClass>/<TestName>/.
 * - Optionally deletes artifacts for successful test runs.
 *
 * @param recordTestExecutionAndCollectLogs If true, records video and logcat for each test.
 * @param deleteSuccessfulTestRecordings If true, deletes test artifacts for passed tests.
 */
public class TestExecutionWatcher(
    private val recordTestExecutionAndCollectLogs: Boolean = false,
    private val deleteSuccessfulTestRecordings: Boolean = true,
    private val postRecordTimeout: Long = 5_000
) : TestWatcher() {

    private val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val watcherPrefix = "TestExecutionWatcher:"
    private val testArtifactsFolder = "artifacts"
    private lateinit var testVideoFile: File
    private lateinit var baseDir: File
    private lateinit var testName: String
    private lateinit var testFolder: File
    private lateinit var classFolder: File

    private fun folderReallyExists(dir: File): Boolean {
        return try {
            dir.exists() && dir.isDirectory && dir.listFiles() != null
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun starting(description: Description?) {
        if (recordTestExecutionAndCollectLogs) {

            runBlocking(Dispatchers.IO) {
                try {
                    val logcatCommand = "logcat -c"
                    uiAutomation.executeShellCommand(logcatCommand)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            baseDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                testArtifactsFolder
            )
            runBlocking(Dispatchers.IO) {
                if (!folderReallyExists(baseDir)) {
                    baseDir.mkdirs()
                    printInfo("$watcherPrefix created folder for test artifacts: ${baseDir.absolutePath}")
                } else {
                    printInfo("$watcherPrefix folder already exists: ${baseDir.absolutePath}")
                }
            }

            classFolder = File(baseDir, description!!.className.substringAfterLast('.'))
            runBlocking(Dispatchers.IO) {
                if (!folderReallyExists(classFolder)) {
                    classFolder.mkdirs()
                    printInfo("$watcherPrefix created folder for test class: ${classFolder.absolutePath}")
                } else {
                    printInfo("$watcherPrefix folder already exists: ${classFolder.absolutePath}")
                }
            }

            testName = description.methodName.replace("[^a-zA-Z0-9]".toRegex(), "")
            testFolder = File(classFolder, testName)
            runBlocking(Dispatchers.IO) {
                if (!folderReallyExists(testFolder)) {
                    printInfo("$watcherPrefix test case folder doesn't exist: ${testFolder.absolutePath}")
                    testFolder.mkdirs()
                    printInfo("$watcherPrefix created folder for test case: ${testFolder.absolutePath}")
                } else {
                    printInfo("$watcherPrefix test case folder exists ${testFolder.absolutePath}")
                    uiAutomation.executeShellCommand("rm -rf $testFolder")
                    printInfo("$watcherPrefix trying to delete test folder ${testFolder.absolutePath}")
                    var attempts = 0
                    while (testFolder.exists() && attempts < 20) {
                        printInfo("$watcherPrefix attempt $attempts")
                        Thread.sleep(200)
                        attempts++
                    }
                    testFolder.mkdirs()
                    printInfo("$watcherPrefix cleaned up existing folder: ${testFolder.absolutePath}")
                }
            }

            runBlocking(Dispatchers.IO) {
                try {
                    testVideoFile = File(testFolder, "$testName.mp4")
                    uiAutomation.executeShellCommand("screenrecord --time-limit 120 $testVideoFile")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            runBlocking(Dispatchers.IO) {
                try {
                    // Start logcat in background with file output
                    val logcatCommand = "logcat -f $testFolder/logcat_log.txt"
                    uiAutomation.executeShellCommand(logcatCommand)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            printInfo("$watcherPrefix video recording for ${description.methodName} started.")
        }
        printInfo("$watcherPrefix ${description?.methodName} starting")
    }

    override fun finished(description: Description?) {
        if (recordTestExecutionAndCollectLogs) {
            printInfo("$watcherPrefix video recording for ${description?.methodName} finished.")
            printInfo("$watcherPrefix ${description?.methodName} finished")

            val screenShootFile = File(testFolder, "$testName.png")
            takeScreenshot(file = screenShootFile, uiAutomation)

            runBlocking(Dispatchers.IO) {
                try {
                    uiAutomation.executeShellCommand("pkill -l SIGINT logcat")
                    printInfo("$watcherPrefix Done with: pkill -l SIGINT logcat")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            printInfo("$watcherPrefix logcat saved to $testFolder/logcat_log.txt")
        }
        printInfo("${description!!.methodName} finished")
    }

    override fun failed(e: Throwable?, description: Description?) {
        printInfo("$watcherPrefix ${description?.methodName} failed! Exception: ${e!!::class.java.simpleName}")
    }

    override fun succeeded(description: Description?) {
        if (recordTestExecutionAndCollectLogs && deleteSuccessfulTestRecordings) {
            runBlocking(Dispatchers.IO) {
                try {
                    uiAutomation.executeShellCommand("rm -rf ${classFolder.absolutePath}")
                    printInfo("$watcherPrefix deleting successful test folder ${testFolder.absolutePath}")
                    var attempts = 0
                    while (classFolder.exists() && attempts < 20) {
                        printInfo("$watcherPrefix attempt $attempts")
                        Thread.sleep(200)
                        attempts++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        printInfo("$watcherPrefix ${description?.methodName} succeeded!")
    }
}

public fun takeScreenshot(
    file: File,
    uiAutomation: UiAutomation,
    name: String = "test_screenshot"
) {
    uiAutomation.executeShellCommand("screencap -p ${file.path}")
    println("📸 Screenshot saved to: ${file.absolutePath}")
}
