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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.test.android.instrumented.utils.FileUtils
import me.proton.core.test.android.instrumented.utils.FileUtils.clearIdlingResources
import me.proton.core.test.android.instrumented.utils.Shell
import me.proton.core.test.android.instrumented.utils.Shell.clearLogcat
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Class that holds common setUp() and tearDown() functions.
 *
 * @property activityRule an ActivityScenarioRule for the RuleChain
 */
open class ProtonTest(private val activityRule: ActivityScenarioRule<*>) {

    class TestExecutionWatcher : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) = Shell.saveToFile(description)
    }

    @Rule
    @JvmField
    val ruleChain = RuleChain
        .outerRule(testName)
        .around(TestExecutionWatcher())
        .around(activityRule)!!

    @Before
    open fun setUp() {
        FileUtils.prepareArtifactsDir(screenshotPath)
        Intents.init()
        clearLogcat()
        Log.d(testTag, "Starting test execution: ${testName.methodName}")
        Intents.intending(CoreMatchers.not(IntentMatchers.isInternal()))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    open fun tearDown() {
        Intents.release()
        clearIdlingResources()
        Log.d(testTag, "Finished test execution: ${testName.methodName}")
    }

    companion object {
        const val testTag = "ESPRESSO_TEST"
        val screenshotPath = "${getContext().filesDir.path}/artifacts/screenshots"
        val testName = TestName()
        fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext!!
    }
}
