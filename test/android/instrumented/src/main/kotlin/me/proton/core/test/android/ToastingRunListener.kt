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

package me.proton.core.test.android

import android.widget.Toast
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

import org.junit.runner.Description
import org.junit.runner.notification.RunListener

/**
 * Toast the name of each test to the screen to make the test easier to identify in a
 * Firebase video stream.
 */
internal class ToastingRunListener : RunListener() {
    override fun testStarted(description: Description) {
        val testName = description.displayName
        getInstrumentation().runOnMainSync {
            Toast.makeText(getInstrumentation().targetContext, testName, Toast.LENGTH_LONG).show()
        }
    }
}
