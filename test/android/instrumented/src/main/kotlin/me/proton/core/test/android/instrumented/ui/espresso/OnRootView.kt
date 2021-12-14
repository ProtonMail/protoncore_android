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

package me.proton.core.test.android.instrumented.ui.espresso

import androidx.test.espresso.Root
import androidx.test.espresso.matcher.RootMatchers
import me.proton.core.test.android.instrumented.utils.ActivityProvider
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.util.ArrayList

/**
 * Simplifies syntax to apply multiple [RootMatchers] on root view.
 */
class OnRootView {
    private val matchers = ArrayList<Matcher<Root>>()
    private val notCurrentActivityWindow = CoreMatchers.not(ActivityProvider.currentActivity!!.window.decorView)

    fun isPlatformPopUp() = apply {
        matchers.add(RootMatchers.isPlatformPopup())
    }

    fun isDialog() = apply {
        matchers.add(RootMatchers.isDialog())
    }

    fun isFocusable() = apply {
        matchers.add(RootMatchers.isFocusable())
    }

    fun isSystemAlertWindow() = apply {
        matchers.add(RootMatchers.isSystemAlertWindow())
    }

    fun isTouchable() = apply {
        matchers.add(RootMatchers.isTouchable())
    }

    fun withDecorView(view: OnView) = apply {
        matchers.add(RootMatchers.withDecorView(view.viewMatcher()))
    }

    // Can be used to locate toast view.
    fun withNotCurrentActivityDecorView() = apply {
        matchers.add(RootMatchers.withDecorView(notCurrentActivityWindow))
    }

    /** Matcher function should be used when we would like to point which [Root] we wanna operate on. **/
    internal fun matcher(): Matcher<Root> = AllOf.allOf(matchers)
}
