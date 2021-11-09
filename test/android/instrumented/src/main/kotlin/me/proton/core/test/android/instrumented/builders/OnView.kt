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
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.RootMatchers.DEFAULT
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.util.ArrayList

/**
 * Builder like class that allows to write [ViewActions] and [ViewAssertion] for single [View].
 */
class OnView : ConditionWatcher {
    private val matchers: ArrayList<Matcher<View>> = arrayListOf()
    private val rootMatchers: ArrayList<Matcher<Root>> = arrayListOf()

    /** [ViewInteraction] wait. **/
    private fun viewInteraction(viewAssertion: ViewAssertion = matches(ViewMatchers.isDisplayed())): ViewInteraction {
        waitForCondition({ onView(viewMatcher()).inRoot(rootMatcher()).check(viewAssertion) })
        return onView(viewMatcher()).inRoot(rootMatcher())
    }

    /** Matcher wrappers **/
    fun instanceOf(clazz: Class<*>?) = apply {
        matchers.add(CoreMatchers.instanceOf(clazz))
    }

    fun isEnabled() = apply {
        matchers.add(ViewMatchers.isEnabled())
    }

    fun hasSibling(siblingView: OnView) = apply {
        matchers.add(ViewMatchers.hasSibling(siblingView.viewMatcher()))
    }

    fun withId(@IdRes id: Int) = apply {
        matchers.add(ViewMatchers.withId(id))
    }

    fun withParent(parentView: OnView) = apply {
        matchers.add(ViewMatchers.withParent(parentView.viewMatcher()))
    }

    fun withText(@StringRes textId: Int) = apply {
        matchers.add(ViewMatchers.withText(stringFromResource(textId)))
    }

    fun withText(text: String) = apply {
        matchers.add(ViewMatchers.withText(text))
    }

    fun startsWith(text: String) = apply {
        matchers.add(ViewMatchers.withText(CoreMatchers.startsWith(text)))
    }

    fun isClickable() = apply {
        matchers.add(ViewMatchers.isClickable())
    }

    fun isChecked() = apply {
        matchers.add(ViewMatchers.isChecked())
    }

    fun isCompletelyDisplayed() = apply {
        matchers.add(ViewMatchers.isCompletelyDisplayed())
    }

    fun isDescendantOf(ancestorView: OnView) = apply {
        matchers.add(ViewMatchers.isDescendantOfA(ancestorView.viewMatcher()))
    }

    fun isDisplayingAtLeast(displayedPercentage: Int) = apply {
        matchers.add(ViewMatchers.isDisplayingAtLeast(displayedPercentage))
    }

    fun isDisabled() = apply {
        matchers.add(CoreMatchers.not(ViewMatchers.isEnabled()))
    }

    fun isFocusable() = apply {
        matchers.add(ViewMatchers.isFocusable())
    }

    fun isFocused() = apply {
        matchers.add(ViewMatchers.isFocused())
    }

    /**
     * Match EditText with [id] or child EditText of ProtonInput with [id].
     */
    fun isInputField(@IdRes id: Int) = apply {
        matchers.add(inputFieldMatcher(id))
    }

    fun isNotChecked() = apply {
        matchers.add(ViewMatchers.isNotChecked())
    }

    fun isSelected() = apply {
        matchers.add(ViewMatchers.isSelected())
    }

    fun hasChildCount(childCount: Int) = apply {
        matchers.add(ViewMatchers.hasChildCount(childCount))
    }

    fun hasContentDescription() = apply {
        matchers.add(ViewMatchers.hasContentDescription())
    }

    fun hasDescendant(descendantView: OnView) = apply {
        matchers.add(ViewMatchers.hasDescendant(descendantView.viewMatcher()))
    }

    fun hasErrorText(errorText: String) = apply {
        matchers.add(ViewMatchers.hasErrorText(errorText))
    }

    fun hasFocus() = apply {
        matchers.add(ViewMatchers.hasFocus())
    }

    fun hasImeAction(imeAction: Int) = apply {
        matchers.add(ViewMatchers.hasImeAction(imeAction))
    }

    fun hasLinks() = apply {
        matchers.add(ViewMatchers.hasLinks())
    }

    fun supportsInputMethods() = apply {
        matchers.add(ViewMatchers.supportsInputMethods())
    }

    fun withChild(childMatcher: OnView) = apply {
        matchers.add(ViewMatchers.withChild(childMatcher.viewMatcher()))
    }

    fun withClassName(className: String) = apply {
        matchers.add(ViewMatchers.withClassName(CoreMatchers.equalTo(className)))
    }

    fun withContentDesc(contentDescText: String) = apply {
        matchers.add(ViewMatchers.withContentDescription(contentDescText))
    }

    fun withContentDesc(@StringRes contentDescTextId: Int) = apply {
        matchers.add(
            ViewMatchers.withContentDescription(
                stringFromResource(contentDescTextId)
            )
        )
    }

    fun withContentDesc(contentDescMatcher: Matcher<out CharSequence?>?) = apply {
        matchers.add(ViewMatchers.withContentDescription(contentDescMatcher))
    }

    fun withHint(hint: String) = apply {
        matchers.add(ViewMatchers.withHint(hint))
    }

    fun withHint(@StringRes hintId: Int) = apply {
        matchers.add(ViewMatchers.withHint(hintId))
    }

    fun withInputType(inputType: Int) = apply {
        matchers.add(ViewMatchers.withInputType(inputType))
    }

    fun withParentIndex(indexInParent: Int) = apply {
        matchers.add(ViewMatchers.withParentIndex(indexInParent))
    }

    fun withResourceName(resourceName: String) = apply {
        matchers.add(ViewMatchers.withResourceName(resourceName))
    }

    fun withSubstring(substring: String) = apply {
        matchers.add(ViewMatchers.withSubstring(substring))
    }

    fun withSpinnerText(spinnerText: String) = apply {
        matchers.add(ViewMatchers.withSpinnerText(spinnerText))
    }

    fun withTag(tag: Any) = apply {
        matchers.add(ViewMatchers.withTagValue(CoreMatchers.`is`(tag)))
    }

    fun withTagKey(tagKey: Int) = apply {
        matchers.add(ViewMatchers.withTagKey(tagKey))
    }

    fun withVisibility(visibility: ViewMatchers.Visibility) = apply {
        matchers.add(ViewMatchers.withEffectiveVisibility(visibility))
    }

    fun withCustomMatcher(matcher: Matcher<View>) = apply {
        matchers.add(matcher)
    }

    fun withRootMatcher(matcher: Matcher<Root>) = apply {
        rootMatchers.add(matcher)
    }

    /** Final [Matcher] for the view. **/
    fun viewMatcher(): Matcher<View> = AllOf.allOf(matchers)

    /** Final [Matcher] for the root. **/
    private fun rootMatcher(): Matcher<Root> = if (rootMatchers.isEmpty()) DEFAULT else AllOf.allOf(rootMatchers)

    /** Action wrappers. **/
    fun click() = apply {
        viewInteraction().perform(ViewActions.click())
    }

    fun clearText() = apply {
        viewInteraction().perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard())
    }

    fun customAction(vararg customViewActions: ViewAction) = apply {
        viewInteraction().perform(*customViewActions)
    }

    fun replaceText(text: String) = apply {
        viewInteraction().perform(ViewActions.replaceText(text), ViewActions.closeSoftKeyboard())
    }

    fun swipeDown() = apply {
        viewInteraction().perform(ViewActions.swipeDown())
    }

    fun swipeLeft() = apply {
        viewInteraction().perform(ViewActions.swipeLeft())
    }

    fun swipeRight() = apply {
        viewInteraction().perform(ViewActions.swipeRight())
    }

    fun swipeUp() = apply {
        viewInteraction().perform(ViewActions.swipeUp())
    }

    fun typeText(text: String) = apply {
        viewInteraction().perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())
    }

    fun closeKeyboard() = apply {
        viewInteraction().perform(ViewActions.closeSoftKeyboard())
    }

    fun closeDrawer() = apply {
        viewInteraction().perform(DrawerActions.close())
    }

    fun doubleClick() = apply {
        viewInteraction().perform(ViewActions.doubleClick())
    }

    fun longClick() = apply {
        viewInteraction().perform(ViewActions.longClick())
    }

    fun openDrawer() = apply {
        viewInteraction().perform(DrawerActions.open())
    }

    fun pressBack() = apply {
        viewInteraction().perform(ViewActions.pressBack())
    }

    fun pressImeActionBtn() = apply {
        viewInteraction().perform(ViewActions.pressImeActionButton())
    }

    fun scrollTo() = apply {
        viewInteraction().perform(ViewActions.scrollTo())
    }

    /** Assertion wrappers **/
    fun checkIsChecked() = apply {
        viewInteraction(matches(ViewMatchers.isChecked()))
    }

    fun checkIsNotChecked() = apply {
        viewInteraction(matches(CoreMatchers.not(ViewMatchers.isChecked())))
    }

    fun checkDisplayed() = apply {
        viewInteraction(matches(ViewMatchers.isDisplayed()))
    }

    fun checkDisabled() = apply {
        viewInteraction(matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    fun checkEnabled() = apply {
        viewInteraction(matches(ViewMatchers.isEnabled()))
    }

    fun checkSelected() = apply {
        viewInteraction(matches(ViewMatchers.isSelected()))
    }

    fun checkContains(text: String) = apply {
        viewInteraction(matches(ViewMatchers.withText(CoreMatchers.containsString(text))))
    }

    fun checkContains(@StringRes textId: Int) = apply {
        viewInteraction(matches(ViewMatchers.withText(stringFromResource(textId))))
    }

    fun checkDoesNotExist() = apply {
        viewInteraction(doesNotExist())
    }

    fun checkNotDisplayed() = apply {
        viewInteraction(matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }
}
