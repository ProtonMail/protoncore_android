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

package me.proton.core.test.android.libtests.report

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.MainInitializer
import me.proton.core.report.test.MinimalReportInternalTests
import me.proton.core.report.test.robot.ReportRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.payments.TestPaymentMethods
import me.proton.core.test.rule.annotation.payments.annotationTestData
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import me.proton.test.fusion.FusionConfig
import org.junit.Rule
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
open class ReportInternalTests : MinimalReportInternalTests {
    @get:Rule
    val protonRule: ProtonRule = protonActivityScenarioRule<MainActivity> (
        annotationTestData = setOf(TestPaymentMethods().annotationTestData),
        afterHilt = {
            MainInitializer.init(it.targetContext)
            FusionConfig.Compose.waitTimeout.set(60.seconds)
            FusionConfig.Espresso.waitTimeout.set(60.seconds)
        }
    )

    override fun startReport() {
        CoreexampleRobot().bugReport()
    }

    override fun verifyAfter() {
        ReportRobot.thankYouDisplayed()
    }
}
