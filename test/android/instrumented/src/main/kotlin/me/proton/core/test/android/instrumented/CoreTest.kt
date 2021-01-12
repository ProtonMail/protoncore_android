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

package me.proton.core.test.android.instrumented

import android.app.Activity
import android.app.Instrumentation
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import me.proton.core.test.android.instrumented.devicesetup.DeviceSetup.clearLogcat
import me.proton.core.test.android.instrumented.failurehandler.ProtonFailureHandler
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.rules.TestName

/**
 * Class that holds common setUp() and tearDown() functions.
 */
open class CoreTest {

    @Before
    open fun setUp() {
        Espresso.setFailureHandler(ProtonFailureHandler(InstrumentationRegistry.getInstrumentation()))
        PreferenceManager.getDefaultSharedPreferences(targetContext).edit().clear().apply()
        Intents.init()
        clearLogcat()
        Log.d(testTag, "Starting test execution for test: ${testName.methodName}")
        Intents.intending(CoreMatchers.not(IntentMatchers.isInternal()))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    open fun tearDown() {
        Intents.release()
        for (idlingResource in IdlingRegistry.getInstance().resources) {
            if (idlingResource == null) {
                continue
            }
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
        Log.d(testTag, "Finished test execution: ${testName.methodName}")
    }

    companion object {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext!!
        val artifactsPath = "${targetContext.filesDir.path}/artifacts"
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation!!
        val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        const val testTag = "PROTON_UI_TEST"
        const val testApp = "testApp"
        const val testRailRunId = "testRailRunId"
        const val downloadArtifactsPath = "/sdcard/Download/artifacts"
        val testName = TestName()
    }
}

