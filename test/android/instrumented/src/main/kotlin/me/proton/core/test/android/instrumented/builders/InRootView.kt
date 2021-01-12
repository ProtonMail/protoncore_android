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

package me.proton.core.test.android.instrumented.builders

import androidx.test.espresso.Root
import androidx.test.espresso.matcher.RootMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.util.ArrayList

/**
 * Simplifies syntax to apply multiple [RootMatchers] on root view.
 */
class InRootView {
    private val matchers = ArrayList<Matcher<Root>>()

    fun isPlatformPopUp(): InRootView = apply { matchers.add(RootMatchers.isPlatformPopup()) }

    fun isDialog(): InRootView = apply { matchers.add(RootMatchers.isDialog()) }

    fun isFocusable(): InRootView = apply { matchers.add(RootMatchers.isFocusable()) }

    fun isSystemAlertWindow(): InRootView = apply { matchers.add(RootMatchers.isSystemAlertWindow()) }

    fun isTouchable(): InRootView = apply { matchers.add(RootMatchers.isTouchable()) }

    /** Matcher function should be used when we would like to point which [Root] we wanna operate on. **/
    fun matcher(): Matcher<Root> = AllOf.allOf(matchers)
}
