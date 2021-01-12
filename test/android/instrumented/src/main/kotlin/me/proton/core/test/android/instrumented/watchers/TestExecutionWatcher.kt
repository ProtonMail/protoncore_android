/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package me.proton.core.test.android.instrumented.watchers

import me.proton.core.test.android.instrumented.CoreTest
import me.proton.core.test.android.instrumented.CoreTest.Companion.targetContext
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

/**
 * Monitors test run results and performs actions on Success or on Failure.
 */
class TestExecutionWatcher : TestWatcher() {

    override fun failed(e: Throwable?, description: Description?) {

        val logcatFile = File(CoreTest.artifactsPath, "${description?.methodName}-logcat.txt")
        CoreTest.automation.executeShellCommand("run-as ${targetContext.packageName} -d -f $logcatFile")
    }
}
