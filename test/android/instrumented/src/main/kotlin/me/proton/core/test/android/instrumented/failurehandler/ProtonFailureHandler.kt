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

package me.proton.core.test.android.instrumented.failurehandler

import android.app.Instrumentation
import android.view.View
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.base.DefaultFailureHandler
import com.jraska.falcon.Falcon
import me.proton.core.test.android.instrumented.CoreTest.Companion.artifactsPath
import me.proton.core.test.android.instrumented.CoreTest.Companion.testName
import me.proton.core.test.android.instrumented.utils.ActivityProvider
import me.proton.core.test.android.instrumented.watchers.ProtonWatcher
import org.hamcrest.Matcher
import java.io.File

/**
 * Handles the error before it is delegated to Espresso [FailureHandler].
 */
class ProtonFailureHandler(instrumentation: Instrumentation) : FailureHandler {

    private val delegate: FailureHandler

    init {
        delegate = DefaultFailureHandler(instrumentation.targetContext)
    }

    override fun handle(error: Throwable, viewMatcher: Matcher<View>) {
        if (ProtonWatcher.status == ProtonWatcher.CONDITION_NOT_MET) {
            // Just delegate as we are in the condition check loop.
            delegate.handle(error, viewMatcher)
        } else {
            // At this point condition is not met - take the screenshot.
            val file = File(artifactsPath, "${testName.methodName}-screenshot.png")
            Falcon.takeScreenshot(ActivityProvider.currentActivity, file)
            delegate.handle(error, viewMatcher)
        }
    }
}

