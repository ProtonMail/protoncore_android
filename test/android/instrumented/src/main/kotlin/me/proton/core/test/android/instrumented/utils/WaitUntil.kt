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

package me.proton.core.test.android.instrumented.utils

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.test.android.instrumented.ProtonTest

/**
 * Waits until [conditionBlock] is true
 * @throws Exception which was last caught during condition check after given [watchTimeout] ms
 */

fun waitUntil(
    watchTimeout: Long = ProtonTest.commandTimeout,
    watchInterval: Long = 250L,
    conditionBlock: () -> Boolean,
) = runBlocking {
    runCatching {
        withTimeoutOrNull(watchTimeout) {
            while (true) {
                if (!conditionBlock()) delay(watchInterval) else break
            }
        }
    }.onFailure {
        Log.d(ProtonTest.testTag, "Test \"${ProtonTest.testName.methodName}\" failed. Saving screenshot")
        Shell.takeScreenshot()
        throw it
    }
}
