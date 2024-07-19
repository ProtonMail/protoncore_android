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
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.test.android.TestWebServerDispatcher
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import timber.log.Timber
import javax.inject.Inject

interface BaseMockTest {

    val testUsername get() = "test-mock-936"
    val testPassword get() = "password"

    /**
     * Perform common setup tasks for tests.
     */
    fun commonSetup()

    /**
     * Perform common teardown tasks for tests.
     */
    fun commonTeardown()
}

/** A minimal example for writing a test with hilt-testing and mocked server. */
@HiltAndroidTest
open class SampleMockTest : BaseMockTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @BaseProtonApiUrl
    lateinit var baseProtonApiUrl: HttpUrl

    @Inject
    lateinit var webServer: MockWebServer

    @Inject
    lateinit var dispatcher: TestWebServerDispatcher

    @Before
    fun setup() {
        commonSetup()
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        commonTeardown()
    }

    override fun commonSetup() {
        initLogging()
    }

    override fun commonTeardown() {
        webServer.shutdown()
    }

    private fun initLogging() {
        Timber.plant(Timber.DebugTree())
        CoreLogger.set(TimberLogger)
    }
}
