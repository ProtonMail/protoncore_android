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

package me.proton.core.test.android.instrumented.waits

import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResourceTimeoutException
import androidx.test.espresso.NoMatchingRootException
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.core.test.android.instrumented.CoreTest.Companion.targetContext
import me.proton.core.test.android.instrumented.utils.ActivityProvider.currentActivity
import me.proton.core.test.android.instrumented.watchers.ProtonWatcher
import org.hamcrest.Matcher

/**
 * Contains different wait functions and retry actions.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
object UIWaits {

    const val TIMEOUT_10S = 10_000L

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
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {
            val errorMessage = "UIWaits.waitUntilMatcherFulfilled "

            override fun getDescription() = errorMessage

            override fun checkCondition(): Boolean {
                return checkViewInteraction(
                    { interaction.check(assertion) },
                    { errorMessage.plus(it) }
                )
            }
        })
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
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {
            val errorMessage = "UIWaits.waitUntilMatcherFulfilled "

            override fun getDescription() = errorMessage

            override fun checkCondition(): Boolean {
                return checkViewInteraction(
                    { interaction.check(assertion) },
                    { errorMessage.plus(it) }
                )
            }
        })
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
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {
            val errorMessage = "UIWaits.waitUntilMatcherFulfilled "

            override fun getDescription() = errorMessage

            override fun checkCondition(): Boolean {
                return checkViewInteraction(
                    { interaction.perform(action) },
                    { errorMessage.plus(it) }
                )
            }
        })
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
        timeout: Long = 10_000
    ): ViewInteraction {
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {
            val errorMessage = "UIWaits.waitUntilMatcherFulfilled "

            override fun getDescription() = errorMessage

            override fun checkCondition(): Boolean {
                interaction.perform(action)
                return checkViewInteraction(
                    { Espresso.onView(matcher).check(assertion) },
                    { errorMessage.plus(it) }
                )
            }
        })
        return interaction
    }

    fun waitUntilRecyclerViewPopulated(@IdRes id: Int, timeout: Long = TIMEOUT_10S) {
        ProtonWatcher.setTimeout(timeout)
        ProtonWatcher.waitForCondition(object : ProtonWatcher.Condition {

            val timedOutResources = ArrayList<String>()

            override fun getDescription() =
                "RecyclerView: ${targetContext.resources.getResourceName(id)} was not populated with items"

            override fun checkCondition() = try {
                val rv = currentActivity!!.findViewById<RecyclerView>(id)
                if (rv != null) {
                    waitUntilLoaded { rv }
                    rv.adapter!!.itemCount > 0
                } else {
                    if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                        timedOutResources.add(getDescription())
                        throw IdlingResourceTimeoutException(timedOutResources)
                    } else {
                        false
                    }
                }
            } catch (e: Throwable) {
                timedOutResources.add(getDescription())
                timedOutResources.add(e.stackTrace.toString())
                throw IdlingResourceTimeoutException(timedOutResources)
            }
        })
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

    private fun checkViewInteraction(conditionBlock: () -> Unit, errorBlock: (errorMessage: String) -> Unit): Boolean {
        return try {
            conditionBlock()
            true
        } catch (e: PerformException) {
            errorBlock("View: \"${e.viewDescription}\", Action: \"${e.actionDescription}\"")
            if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                throw e
            } else {
                false
            }
        } catch (e: NoMatchingViewException) {
            errorBlock("View Matcher: \"${e.viewMatcherDescription}\"")
            if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                throw e
            } else {
                false
            }
        } catch (e: NoMatchingRootException) {
            errorBlock("Unable to match Root View: \"${e.message}\"")
            if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                throw e
            } else {
                false
            }
        } catch (t: Throwable) {
            if (ProtonWatcher.status == ProtonWatcher.TIMEOUT) {
                throw t
            } else {
                false
            }
        }
    }
}
