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

package me.proton.core.test.android.mockuitests

import dagger.hilt.android.testing.HiltAndroidRule
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.android.instrumented.utils.Shell
import me.proton.core.test.android.mocks.FakeApiClient
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

/** A test rule that applies [HiltAndroidRule] and prepares a mock [MockWebServer].
 * @see SampleMockTest
 */
class MockTestRule constructor(private val testInstance: BaseMockTest) : TestRule {
    lateinit var dispatcher: TestWebServerDispatcher

    override fun apply(base: Statement, description: Description): Statement {
        ProtonTest.commandTimeout = FakeApiClient.CALL_TIMEOUT.inWholeMilliseconds
        initLogging()

        dispatcher = TestWebServerDispatcher()
        val webServer = MockWebServer()
        webServer.dispatcher = dispatcher

        val hiltRule = HiltAndroidRule(testInstance)
        val result = hiltRule.apply(object : Statement() {
            override fun evaluate() {
                testInstance.baseProtonApiUrl = webServer.url("/")
                hiltRule.inject()
                base.evaluate()
            }
        }, description)

        return object : Statement() {
            override fun evaluate() {
                try {
                    result.evaluate()
                } catch (t: Throwable) {
                    Shell.saveToFile(description)
                    throw t
                } finally {
                    webServer.shutdown()
                }
            }
        }
    }

    private fun initLogging() {
        Timber.plant(Timber.DebugTree())
        CoreLogger.set(TimberLogger)
    }
}
