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
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.TIMEOUT_10S
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitForView
import me.proton.core.test.android.instrumented.uiwaits.UIWaits.waitUntilViewIsGone
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.util.ArrayList

/**
 * Builder like class that allows to write [ViewActions] and [ViewAssertion] for single [View].
 */
class OnView {
    private var id: Int? = null
    private var text: String? = null
    private var parentMatcher: Matcher<View>? = null
    private var clazz: Class<*>? = null
    private var viewMatcher: Matcher<View>? = null
    private var tag: Any? = null
    private var hint: String? = null
    private var hintId: Int? = null
    private var visibility: ViewMatchers.Visibility? = null
    private var contentDescMatcher: Matcher<out CharSequence?>? = null
    private var contentDescText: String? = null
    private var contentDescTextId: Int? = null
    private var ancestorMatcher: Matcher<View>? = null


    /** [View] properties. **/
    fun withId(@IdRes id: Int) = apply { this.id = id }

    fun withText(@StringRes textId: Int) = apply { this.text = stringFromResource(textId) }

    fun withText(text: String) = apply { this.text = text }

    fun withParent(parentView: OnView) = apply { this.parentMatcher = parentView.matcher() }

    fun instanceOf(clazz: Class<*>?) = apply { this.clazz = clazz }

    fun isDescendantOf(ancestorView: OnView) = apply { this.viewMatcher = ancestorView.matcher() }

    fun withTag(tag: Any) = apply { this.tag = tag }

    fun withHint(hint: String) = apply { this.hint = hint }

    fun withHint(@StringRes hintId: Int) = apply { this.hintId = hintId }

    fun withContentDesc(contentDescText: String) = apply { this.contentDescText = contentDescText }

    fun withContentDesc(@StringRes contentDescTextId: Int) = apply { this.contentDescTextId = contentDescTextId }

    fun withContentDesc(contentDescMatcher: Matcher<out CharSequence?>?) =
        apply { this.contentDescMatcher = contentDescMatcher }

    fun withVisibility(visibility: ViewMatchers.Visibility) = apply { this.visibility = visibility }


    /** [ViewInteraction] action wrappers. **/
    fun click() = apply { waitForView(viewInteraction()).perform(ViewActions.click()) }

    fun clearText() = apply {
        waitForView(viewInteraction()).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard())
    }

    fun closeDrawer() = apply { waitForView(viewInteraction()).perform(DrawerActions.close()) }

    fun closeKeyboard() = apply { waitForView(viewInteraction()).perform(ViewActions.closeSoftKeyboard()) }

    fun doubleClick() = apply { waitForView(viewInteraction()).perform(ViewActions.doubleClick()) }

    fun longClick() = apply { waitForView(viewInteraction()).perform(ViewActions.longClick()) }

    fun openDrawer() = apply { waitForView(viewInteraction()).perform(DrawerActions.open()) }

    fun pressBack() = apply { waitForView(viewInteraction()).perform(ViewActions.pressBack()) }

    fun pressImeActionBtn() = apply { waitForView(viewInteraction()).perform(ViewActions.pressImeActionButton()) }

    fun replaceText(text: String) = apply {
        waitForView(viewInteraction()).perform(ViewActions.replaceText(text), ViewActions.closeSoftKeyboard())
    }

    fun scrollTo() = apply { waitForView(viewInteraction()).perform(ViewActions.scrollTo()) }

    fun swipeDown() = apply { waitForView(viewInteraction()).perform(ViewActions.swipeDown()) }

    fun swipeLeft() = apply { waitForView(viewInteraction()).perform(ViewActions.swipeLeft()) }

    fun swipeRight() = apply { waitForView(viewInteraction()).perform(ViewActions.swipeRight()) }

    fun swipeUp() = apply { waitForView(viewInteraction()).perform(ViewActions.swipeUp()) }

    fun typeText(text: String) = apply {
        waitForView(viewInteraction()).perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())
    }


    /** [ViewInteraction] assertion wrappers. **/
    fun checkContains(text: String) = apply {
        waitForView(viewInteraction())
            .check(ViewAssertions.matches(ViewMatchers.withText(CoreMatchers.containsString(text))))
    }

    fun checkDisplayed() = apply { viewInteraction().check(ViewAssertions.matches(ViewMatchers.isDisplayed())) }

    fun checkDisabled() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    fun checkDoesNotExist() = apply { waitUntilViewIsGone(viewInteraction()) }

    fun checkNotDisplayed() = apply {
        viewInteraction().check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }


    /** [ViewInteraction] wait functions. **/
    fun wait(timeout: Long = TIMEOUT_10S) = apply { waitForView(viewInteraction(), timeout) }

    fun waitUntilGone() = apply { waitUntilViewIsGone(viewInteraction()) }


    /** Indicates that [View] is part of specific root view. **/
    fun inRoot(rootView: InRootView) = apply { rootMatcher = rootView.matcher() }


    /** Builds final [Matcher] for the view. **/
    fun matcher(): Matcher<View> = viewMatcher()

    private fun viewInteraction(): ViewInteraction {
        return onView(viewMatcher())
            .apply { inRoot(rootMatcher) }
    }

    private fun viewMatcher(): Matcher<View> {
        val matchers = ArrayList<Matcher<View>>()
        if (id != null) {
            matchers.add(ViewMatchers.withId(id!!))
        }
        if (text != null) {
            matchers.add(ViewMatchers.withText(text))
        }
        if (clazz != null) {
            matchers.add(CoreMatchers.instanceOf(clazz))
        }
        if (ancestorMatcher != null) {
            matchers.add(ViewMatchers.isDescendantOfA(ancestorMatcher))
        }
        if (tag != null) {
            matchers.add(ViewMatchers.withTagValue(`is`(tag)))
        }
        if (hint != null) {
            matchers.add(ViewMatchers.withHint(hint))
        }
        if (visibility != null) {
            matchers.add(ViewMatchers.withEffectiveVisibility(visibility))
        }
        if (parentMatcher != null) {
            matchers.add(ViewMatchers.withParent(parentMatcher))
        }
        if (contentDescMatcher != null) {
            matchers.add(ViewMatchers.withContentDescription(contentDescMatcher))
        }
        if (contentDescText != null) {
            matchers.add(ViewMatchers.withContentDescription(contentDescText))
        }
        if (contentDescTextId != null) {
            matchers.add(ViewMatchers.withContentDescription(contentDescTextId!!))
        }
        return AllOf.allOf(matchers)
    }

    companion object {
        /** Default rootMatcher value for[OnListView] instance. **/
        private var rootMatcher: Matcher<Root> = RootMatchers.DEFAULT
    }
}
