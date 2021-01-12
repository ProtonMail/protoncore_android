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

package me.proton.core.test.android.instrumented.uiactions

import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.annotation.IdRes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

/**
 * Set of custom [ViewAction]s.
 */
object UIActions {

    fun setNumberPickerValue(num: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController, view: View) {
                (view as NumberPicker).value = num
            }

            override fun getDescription(): String = "Set the passed number into the NumberPicker"

            override fun getConstraints(): Matcher<View> = isAssignableFrom(NumberPicker::class.java)
        }
    }

    /**
     * Can be used to click a child with id in [RecyclerView.ViewHolder].
     */
    fun clickOnChildWithId(@IdRes id: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController, view: View) {
                view.findViewById<View>(id).performClick()
            }

            override fun getDescription(): String = "Click child view with id."

            override fun getConstraints(): Matcher<View> = isAssignableFrom(View::class.java)
        }
    }

    /**
     * Iterates through [RecyclerView.ViewHolder] views and clicks one that matches provided matcher.
     * @param matcher - matcher that matcher a view inside a holder item view.
     */
    fun clickOnMatchedDescendant(matcher: Matcher<View>): ViewAction {
        return object : ViewAction {

            override fun getDescription(): String = "Click child view that matches: \"$matcher\""

            override fun getConstraints(): Matcher<View> = isAssignableFrom(View::class.java)

            override fun perform(uiController: UiController, view: View) {
                if (view is ViewGroup) {
                    view.children.forEach { childView ->
                        if (matcher.matches(childView)) {
                            childView.performClick()
                            return
                        } else {
                            // Recursively call perform() function till we reach the last view.
                            perform(uiController, childView)
                        }
                    }
                }
            }
        }
    }
}
