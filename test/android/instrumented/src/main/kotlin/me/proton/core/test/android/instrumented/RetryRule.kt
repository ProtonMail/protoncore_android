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

import android.app.Activity
import android.util.Log
import androidx.test.core.app.ActivityScenario
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RetryRule(
    private val activity: Class<out Activity>,
    private val tries: Int = 2
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            var throwable: Throwable? = null

            @Throws(Throwable::class)
            override fun evaluate() {
                for (i in 0 until tries) {
                    val run = "${i + 1} / $tries"
                    Log.d(testTag, "Run $run")
                    try {
                        base.evaluate()
                        Log.d(testTag, "Test passed on run $run")
                        return
                    } catch (t: Throwable) {
                        throwable = t
                        Log.e(testTag, "Test failed on run $run:\n ${t.message}")
                        if (i < tries) {
                            ActivityScenario.launch(activity)
                        }
                    }
                }
                throw throwable!!
            }
        }
    }
}
