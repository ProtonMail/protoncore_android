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

import org.junit.rules.TestWatcher
import org.junit.runner.Description

public class TestExecutionWatcher: TestWatcher() {
    override fun starting(description: Description?) {
        printInfo("${description?.methodName} starting")
    }

    override fun finished(description: Description?) {
        printInfo("${description?.methodName} finished")
    }

    override fun failed(e: Throwable?, description: Description?) {
        printInfo("${description?.methodName} failed! Exception: ${e!!::class.java.simpleName}")
    }

    override fun succeeded(description: Description?) {
        printInfo("${description?.methodName} succeeded!")
    }
}
