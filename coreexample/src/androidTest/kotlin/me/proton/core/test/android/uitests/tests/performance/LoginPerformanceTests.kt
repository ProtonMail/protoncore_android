/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.test.android.uitests.tests.performance

import android.util.Log
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.performance.LogcatFilter
import me.proton.core.test.performance.annotation.Measure
import me.proton.core.test.performance.measurement.AppSizeMeasurement
import me.proton.core.test.performance.measurement.DurationMeasurement
import me.proton.core.test.quark.data.User
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.UUID

class LoginPerformanceTests : BaseTest() {
    private val loginRobot = LoginRobot()
    private val user: User = users.getUser { it.isPaid }
    private val coreExampleTestTagOne: String = "coreExampleTestTagOne"
    private val coreExampleTestTagTwo: String = "coreExampleTestTagTwo"
    private val coreExampleTestTagThree: String = "coreExampleTestTagThree"
    private val measurementContext = measurementRule.measurementContext(measurementConfig)

    @Before
    fun signIn() {
        AddAccountRobot()
            .signIn()
            .verify { loginElementsDisplayed() }
    }

    @Test
    @Measure
    fun measureLoginTimeWithLogcatFilter() {
        val logcatFilter = LogcatFilter()
            .addTag(coreExampleTestTagOne)
            .addTag(coreExampleTestTagTwo)
            .addTag(coreExampleTestTagThree)
            .setLokiLogsId(logsId)
            .failTestOnEmptyLogs()

        val profile = measurementContext
            .setWorkflow("coreexample")
            .setServiceLevelIndicator("login_duration")
            .setLogcatFilter(logcatFilter)

        loginRobot
            .username(user.name)
            .password(user.password)
            .clickSignInButton()

        profile.measure {
            Thread.sleep(3000)  //Added for testing purposes. Remove from your code.
            Log.d(coreExampleTestTagOne, "Test logcat log line. One")
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
            Log.d(coreExampleTestTagTwo, "Test logcat log line. Two")
        }
        profile.pushLogcatLogs()
        profile.clearLogcatLogs()
        profile.measure {
            Thread.sleep(3000)  //Added for testing purposes. Remove from your code.
            Log.d(coreExampleTestTagThree, "Test logcat log line. Three")
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
        }
        profile.pushLogcatLogs()
    }

    @Test
    @Measure
    fun measureLoginTimeNoLogcatLogs() {
        loginRobot
            .username(user.name)
            .password(user.password)
            .clickSignInButton()

        val profile = measurementContext
            .setWorkflow("coreexample")
            .setServiceLevelIndicator("login_time")
            .addMeasurement(DurationMeasurement())
            .addMeasurement(AppSizeMeasurement())

        profile.measure {
            CoreexampleRobot().verify { userStateIs(user, Ready, Authenticated) }
        }
    }

    companion object {
        private var logsId: String = ""

        @JvmStatic
        @BeforeClass
        fun setUpLogsId() {
            // Set logsId per test class run so it is easier to search for logs on loki.
            logsId = UUID.randomUUID().toString()
        }
    }
}
