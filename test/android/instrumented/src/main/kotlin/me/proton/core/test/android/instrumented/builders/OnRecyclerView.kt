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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.test.android.instrumented.uiactions.UIActions.clickOnMatchedDescendant
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitForView
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitUntilRecyclerViewPopulated
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitUntilViewIsGone
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

/**
 * Builder like class that simplifies syntax for actions and verifications on [RecyclerView.ViewHolder].
 */
class OnRecyclerView {
    private var id: Int? = null

    class Builder(private val id: Int) {
        private var viewHolderMatcher: Matcher<RecyclerView.ViewHolder>? = null
        private var position: Int? = null
        private var itemChildViewMatcher: Matcher<View>? = null


        /** [RecyclerViewActions] **/
        fun click(): ViewInteraction {
            viewAction =
                /** When itemChildViewMatcher is null use [ViewActions.click]. **/
                itemChildViewMatcher?.let { clickOnMatchedDescendant(itemChildViewMatcher!!) } ?: ViewActions.click()
            return perform()
        }

        fun doubleClick(): ViewInteraction {
            viewAction = ViewActions.doubleClick()
            return perform()
        }

        fun longClick(): ViewInteraction {
            viewAction = ViewActions.longClick()
            return perform()
        }

        fun swipeDown(): ViewInteraction {
            viewAction = ViewActions.swipeDown()
            return perform()
        }

        fun swipeLeft(): ViewInteraction {
            viewAction = ViewActions.swipeLeft()
            return perform()
        }

        fun swipeRight(): ViewInteraction {
            viewAction = ViewActions.swipeRight()
            return perform()
        }

        fun swipeUp(): ViewInteraction {
            viewAction = ViewActions.swipeUp()
            return perform()
        }


        /** Wait functions. **/
        fun wait() = apply { waitForView(viewInteraction()) }

        fun waitUntilGone() = apply { waitUntilViewIsGone(viewInteraction()) }

        fun waitUntilPopulated() = apply {
            waitForView(viewInteraction())
            waitUntilRecyclerViewPopulated(id)
        }


        /** Wrapper for [RecyclerViewActions.scrollToHolder]. **/
        fun scrollToHolder(viewHolderMatcher: Matcher<RecyclerView.ViewHolder>): ViewInteraction =
            Espresso.onView(viewMatcher()).perform(RecyclerViewActions.scrollToHolder(viewHolderMatcher))

        /** Wrapper for [RecyclerViewActions.actionOnHolderItem]. **/
        fun onHolderItem(viewHolderMatcher: Matcher<RecyclerView.ViewHolder>) =
            apply { this.viewHolderMatcher = viewHolderMatcher }

        /** Wrapper for [RecyclerViewActions.actionOnItemAtPosition]. **/
        fun onItemAtPosition(position: Int) =
            apply { this.position = position }

        /** Points to perform an action on [RecyclerView.ViewHolder] child or descendant. **/
        fun onItemChildView(view: OnView) = apply { itemChildViewMatcher = view.matcher() }

        /** [ViewInteraction] for the [RecyclerView] instance. **/
        private fun viewInteraction(): ViewInteraction = Espresso.onView(viewMatcher())

        /** Performs action on [RecyclerView] based on action defined by [OnRecyclerView.Builder]. **/
        private fun perform(): ViewInteraction {
            when {
                viewHolderMatcher != null -> {
                    return waitForView(viewInteraction()).perform(actionOnHolderItem(viewHolderMatcher, viewAction))
                }
                position != null -> {
                    return waitForView(viewInteraction())
                        .perform(
                            actionOnItemAtPosition<RecyclerView.ViewHolder?>(
                                position!!,
                                viewAction
                            )
                        )
                }
                else -> {
                    throw PerformException
                        .Builder()
                        .withActionDescription(
                            "Unable to perform RecyclerView action when " +
                                "viewHolderMatcher and position values are null! viewHolderMatcher or position " +
                                "must be provided."
                        )
                        .build()
                }
            }
        }

        /** [ViewMatchers] to locate [RecyclerView] element in hierarchy. **/
        private fun viewMatcher(): Matcher<View> = AllOf.allOf(ViewMatchers.withId(id))

        companion object {
            /** Default [ViewAction] for [OnRecyclerView] instance. **/
            private var viewAction: ViewAction? = ViewActions.click()
        }
    }

    fun withId(id: Int): Builder {
        this.id = id
        return Builder(id)
    }
}
