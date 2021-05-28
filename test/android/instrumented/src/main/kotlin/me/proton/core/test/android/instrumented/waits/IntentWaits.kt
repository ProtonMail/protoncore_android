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

package me.proton.core.test.android.instrumented.waits

import android.content.Intent
import androidx.test.espresso.intent.Intents
import junit.framework.AssertionFailedError
import me.proton.core.test.android.instrumented.watchers.ProtonWatcher
import org.hamcrest.Matcher

object IntentWaits {

    internal const val TIMEOUT_5S = 5000L

    fun waitUntilIntentMatcherFulfilled(
        matcher: Matcher<Intent>,
        timeout: Long = TIMEOUT_5S
    ) {
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {

            override fun getDescription() = "IntentWaits.waitUntilIntentMatcherFulfilled"

            override fun checkCondition(): Boolean {
                return try {
                    Intents.intended(matcher)
                    true
                } catch (e: AssertionFailedError) {
                    if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                        throw e
                    } else {
                        false
                    }
                }
            }
        })
    }
}
