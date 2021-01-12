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

import android.view.View
import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitForView
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitUntilViewIsGone
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * Builder like class that allows to write [ViewActions] and view assertions for ListView items.
 */
class OnListView {
    private var dataMatcher: Matcher<out Any?>? = null

    class Builder(private val dataMatcher: Matcher<out Any?>) {
        private var position: Int? = null
        private var itemChildViewMatcher: Matcher<View>? = null
        private var adapterMatcher: Matcher<View>? = null


        /** [DataInteraction] actions wrappers. **/
        fun click() = apply { waitForView(dataInteraction()).perform(ViewActions.click()) }

        fun longClick() = apply { waitForView(dataInteraction()).perform(ViewActions.longClick()) }

        fun replaceText(text: String) = apply {
            waitForView(dataInteraction()).perform(ViewActions.replaceText(text), ViewActions.closeSoftKeyboard())
        }

        fun swipeRight() = apply { waitForView(dataInteraction()).perform(ViewActions.swipeRight()) }

        fun swipeLeft() = apply { waitForView(dataInteraction()).perform(ViewActions.swipeLeft()) }

        fun swipeDown() = apply { waitForView(dataInteraction()).perform(ViewActions.swipeDown()) }

        fun swipeUp() = apply { waitForView(dataInteraction()).perform(ViewActions.swipeUp()) }

        fun scrollTo() = apply { waitForView(dataInteraction()).perform(ViewActions.scrollTo()) }

        fun typeText(text: String) = apply {
            waitForView(dataInteraction()).perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())
        }


        /** [DataInteraction] assertions wrappers. **/
        fun checkContains(text: String) = apply {
            waitForView(dataInteraction())
                .check(ViewAssertions.matches(ViewMatchers.withText(CoreMatchers.containsString(text))))
        }

        fun checkDoesNotExist() = apply { waitUntilViewIsGone(dataInteraction()).check(ViewAssertions.doesNotExist()) }

        fun checkDisabled() = apply {
            waitForView(dataInteraction()).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
        }

        fun checkDisplayed() = apply {
            waitForView(dataInteraction()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

        fun checkNotDisplayed() = apply {
            dataInteraction().check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        }


        /** [DataInteraction] matcher functions. **/
        fun atPosition(position: Int): Builder = apply { this.position = position }

        fun inAdapter(adapterView: OnView): Builder = apply { this.adapterMatcher = adapterView.matcher() }

        fun inRoot(rootView: InRootView): Builder = apply { rootMatcher = rootView.matcher() }

        fun onChild(childView: OnView): Builder = apply { this.itemChildViewMatcher = childView.matcher() }


        /** Wait functions. **/
        fun wait(): Builder = apply { waitForView(dataInteraction()) }

        fun waitUntilGone(): DataInteraction = waitUntilViewIsGone(dataInteraction())


        /** Builds [DataInteraction] based on parameters provided to [OnListView.Builder]. **/
        private fun dataInteraction(): DataInteraction {
            return onData(dataMatcher)
                .apply { inRoot(rootMatcher) }
                .apply {
                    if (position != null) {
                        atPosition(position)
                    }
                }.apply {
                    if (adapterMatcher != null) {
                        inAdapterView(adapterMatcher)
                    }
                }.apply {
                    if (itemChildViewMatcher != null) {
                        this.apply { onChildView(itemChildViewMatcher) }
                    }
                }
        }

        companion object {
            /** Default rootMatcher value for[OnListView] instance. **/
            private var rootMatcher: Matcher<Root> = RootMatchers.DEFAULT
        }
    }

    fun onListItem(dataMatcher: Matcher<out Any?>): Builder {
        this.dataMatcher = dataMatcher
        return Builder(dataMatcher)
    }
}
