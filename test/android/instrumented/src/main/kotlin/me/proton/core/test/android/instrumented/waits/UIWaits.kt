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

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResourceTimeoutException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.test.android.instrumented.utils.ActivityProvider.currentActivity
import me.proton.core.test.android.instrumented.waits.ConditionWatcher.Companion.TIMEOUT_10S
import org.hamcrest.Matcher

/**
 * Contains different wait functions and retry actions.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object UIWaits : ConditionWatcher {

    fun waitForView(interaction: ViewInteraction, timeout: Long = TIMEOUT_10S): ViewInteraction =
        waitUntilMatcherFulfilled(
            interaction,
            assertion = matches(isDisplayed()),
            timeout = timeout
        )

    fun waitForView(interaction: DataInteraction, timeout: Long = TIMEOUT_10S): DataInteraction =
        waitUntilMatcherFulfilled(
            interaction,
            assertion = matches(isDisplayed()),
            timeout = timeout
        )

    fun waitUntilViewIsGone(interaction: ViewInteraction, timeout: Long = TIMEOUT_10S): ViewInteraction =
        waitUntilMatcherFulfilled(
            interaction,
            assertion = doesNotExist(),
            timeout = timeout
        )

    fun waitUntilViewIsGone(interaction: DataInteraction, timeout: Long = TIMEOUT_10S): DataInteraction =
        waitUntilMatcherFulfilled(
            interaction,
            assertion = doesNotExist(),
            timeout = timeout
        )

    /**
     * Waits until provided [ViewAssertion] fulfilled.
     * @param interaction - [ViewInteraction] parameter.
     * @param assertion - [ViewAssertion] that should be fulfilled.
     * @param timeout - optional timeout parameter to wait for the assertion fulfillment.
     */
    fun waitUntilMatcherFulfilled(
        interaction: ViewInteraction,
        assertion: ViewAssertion,
        timeout: Long = TIMEOUT_10S
    ): ViewInteraction {
        waitForCondition({ interaction.check(assertion) }, timeout)
        return interaction
    }

    /**
     * Waits until provided [ViewAssertion] fulfilled.
     * @param interaction - [DataInteraction] parameter.
     * @param assertion - [ViewAssertion] that should be fulfilled.
     * @param timeout - optional timeout parameter to wait for the assertion fulfillment.
     */
    fun waitUntilMatcherFulfilled(
        interaction: DataInteraction,
        assertion: ViewAssertion,
        timeout: Long = TIMEOUT_10S
    ): DataInteraction {
        waitForCondition({ interaction.check(assertion) }, timeout)
        return interaction
    }

    /**
     * Tries to perform an action and retries within provided time period if action fails.
     * @param interaction - [ViewInteraction] parameter.
     * @param action - [ViewAction] that should be performed.
     * @param timeout - optional timeout parameter to wait for the assertion fulfillment.
     */
    fun performActionWithRetry(
        interaction: ViewInteraction,
        action: ViewAction,
        timeout: Long = TIMEOUT_10S
    ): ViewInteraction {
        waitForCondition({ interaction.perform(action) }, timeout)
        return interaction
    }

    /**
     * Tries to perform an action until [ViewAssertion] fulfilled.
     * @param interaction - [ViewInteraction] parameter.
     * @param assertion - [ViewAssertion] that should be fulfilled.
     * @param matcher - [Matcher] object that should be matched.
     * @param action - [ViewAction] that should be performed.
     * @param timeout - optional timeout parameter to wait for the assertion fulfillment.
     */
    fun performActionUntilMatcherFulfilled(
        interaction: ViewInteraction,
        assertion: ViewAssertion,
        matcher: Matcher<View>,
        action: ViewAction,
        timeout: Long = TIMEOUT_10S
    ): ViewInteraction {
        waitForCondition(
            {
                interaction.perform(action)
                Espresso.onView(matcher).check(assertion)
            },
            timeout
        )
        return interaction
    }

    fun waitUntilRecyclerViewPopulated(@IdRes id: Int, timeout: Long = TIMEOUT_10S) {
        val timedOutResources = ArrayList<String>()

        waitForCondition(
            {
                try {
                    val rv = currentActivity!!.findViewById<RecyclerView>(id)
                    if (rv != null) {
                        waitUntilLoaded { rv }
                        rv.adapter!!.itemCount > 0
                    }
                } catch (e: Throwable) {
                    timedOutResources.add(e.stackTrace.toString())
                    throw IdlingResourceTimeoutException(timedOutResources)
                }
            },
            timeout
        )
    }

    /**
     * Stop the test until RecyclerView's data gets loaded.
     * Passed [recyclerProvider] will be activated in UI thread, allowing you to retrieve the View.
     * Workaround for https://issuetracker.google.com/issues/123653014.
     */
    inline fun waitUntilLoaded(crossinline recyclerProvider: () -> RecyclerView) {
        Espresso.onIdle()
        lateinit var recycler: RecyclerView

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            recycler = recyclerProvider()
        }

        while (recycler.hasPendingAdapterUpdates()) {
            Thread.sleep(10)
        }
    }
}
