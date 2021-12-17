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

package me.proton.core.test.android.instrumented

import android.util.Log
import me.proton.core.test.android.instrumented.ProtonTest.Companion.commandTimeout
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testName
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag
import me.proton.core.test.android.instrumented.utils.FileUtils
import java.util.concurrent.TimeoutException

interface ConditionWatcher {

    /**
     * Waits until [conditionBlock] does not throw any exceptions
     * @throws Exception which was last caught during condition check after given [watchTimeout] ms
     */

    fun waitForCondition(
        conditionBlock: () -> Unit,
        watchTimeout: Long = commandTimeout,
        watchInterval: Long = 250L,
    ) {
        var throwable: Throwable = TimeoutException("Condition was not met in $watchTimeout ms. No exceptions caught.")
        var currentTimestamp = System.currentTimeMillis()
        val timeoutTimestamp = currentTimestamp + commandTimeout

        while (currentTimestamp < timeoutTimestamp) {
            currentTimestamp = System.currentTimeMillis()
            try {
                return conditionBlock()
            } catch (e: Throwable) {
                val firstLine = e.message?.split("\n")?.get(0)
                Log.v(testTag, "Waiting for condition. ${timeoutTimestamp - currentTimestamp}ms remaining. Status: $firstLine")
                throwable = e
            }
            Thread.sleep(watchInterval)
        }
        Log.d(testTag, "Test \"${testName.methodName}\" failed. Saving screenshot")
        FileUtils.takeScreenshot()
        throw throwable
    }
}
