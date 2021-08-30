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
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.test.android.instrumented.matchers.SystemUI
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.instrumented.waits.ConditionWatcher.Companion.TIMEOUT_10S
import me.proton.core.test.android.instrumented.waits.UIWaits.waitForView
import me.proton.core.test.android.instrumented.waits.UIWaits.waitUntilViewIsGone
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.util.ArrayList

/**
 * Builder like class that allows to write [ViewActions] and [ViewAssertion] for single [View].
 */
@Suppress("HasPlatformType")
class OnView {
    private var tag: Any? = null

    private var isCompletelyDisplayed: Boolean = false
    private var hasLinks: Boolean = false
    private var hasFocus: Boolean = false
    private var hasContentDescription: Boolean = false
    private var isClickable: Boolean = false
    private var isChecked: Boolean = false
    private var isDisabled: Boolean = false
    private var isEnabled: Boolean = false
    private var isFocusable: Boolean = false
    private var isFocused: Boolean = false
    private var isNotChecked: Boolean = false
    private var isSelected: Boolean = false
    private var supportsInputMethods: Boolean = false

    private var clazz: Class<*>? = null

    private var childCount: Int? = null
    private var displayedPercentage: Int? = null
    private var hintId: Int? = null
    private var inputType: Int? = null
    private var imeAction: Int? = null
    private var id: Int? = null
    private var tagKey: Int? = null

    private var className: String? = null
    private var contentDescText: String? = null
    private var contentDescTextId: Int? = null
    private var errorText: String? = null
    private var hint: String? = null
    private var resourceName: String? = null
    private var spinnerText: String? = null
    private var substring: String? = null
    private var text: String? = null
    private var startsWith: String? = null
    private var indexInParent: Int? = null

    private var ancestorMatcher: Matcher<View>? = null
    private var childMatcher: Matcher<View>? = null
    private var descendantMatcher: Matcher<View>? = null
    private var parentMatcher: Matcher<View>? = null
    private var siblingMatcher: Matcher<View>? = null

    private var contentDescMatcher: Matcher<out CharSequence?>? = null

    private var customMatcher: Matcher<View>? = null

    private var visibility: ViewMatchers.Visibility? = null

    val positiveDialogButton = SystemUI.positiveDialogBtn
    val neutralDialogBtn = SystemUI.neutralDialogBtn
    val negativeDialogBtn = SystemUI.negativeDialogBtn
    val moreOptionsBtn = SystemUI.moreOptionsBtn

    /** [View] properties. **/
    fun instanceOf(clazz: Class<*>?) = apply { this.clazz = clazz }

    fun isClickable() = apply { this.isClickable = true }

    fun isChecked() = apply { this.isChecked = true }

    fun isCompletelyDisplayed() = apply { this.isCompletelyDisplayed = true }

    fun isDescendantOf(ancestorView: OnView) = apply { this.ancestorMatcher = ancestorView.matcher() }

    fun isDisplayingAtLeast(displayedPercentage: Int) = apply { this.displayedPercentage = displayedPercentage }

    fun isDisabled() = apply { this.isDisabled = true }

    fun isEnabled() = apply { this.isEnabled = true }

    fun isFocusable() = apply { this.isFocusable = true }

    fun isFocused() = apply { this.isFocused = true }

    fun isNotChecked() = apply { this.isNotChecked = true }

    fun isSelected() = apply { this.isSelected = true }

    fun hasChildCount(childCount: Int) = apply { this.childCount = childCount }

    fun hasContentDescription() = apply { this.hasContentDescription = true }

    fun hasDescendant(descendantView: OnView) = apply { this.descendantMatcher = descendantView.matcher() }

    fun hasErrorText(errorText: String) = apply { this.errorText = errorText }

    fun hasFocus() = apply { this.hasFocus = true }

    fun hasImeAction(imeAction: Int) = apply { this.imeAction = imeAction }

    fun hasLinks() = apply { this.hasLinks = true }

    fun hasSibling(siblingView: OnView) = apply { this.siblingMatcher = siblingView.matcher() }

    fun supportsInputMethods() = apply { this.supportsInputMethods = true }

    fun withChild(childMatcher: OnView) = apply { this.childMatcher = childMatcher.matcher() }

    fun withClassName(className: String) = apply { this.className = className }

    fun withContentDesc(contentDescText: String) = apply { this.contentDescText = contentDescText }

    fun withContentDesc(@StringRes contentDescTextId: Int) = apply { this.contentDescTextId = contentDescTextId }

    fun withContentDesc(contentDescMatcher: Matcher<out CharSequence?>?) =
        apply { this.contentDescMatcher = contentDescMatcher }

    fun withHint(hint: String) = apply { this.hint = hint }

    fun withHint(@StringRes hintId: Int) = apply { this.hintId = hintId }

    fun withId(@IdRes id: Int) = apply { this.id = id }

    fun withInputType(inputType: Int) = apply { this.inputType = inputType }

    fun withParent(parentView: OnView) = apply { this.parentMatcher = parentView.matcher() }

    fun withParentIndex(indexInParent: Int) = apply { this.indexInParent = indexInParent }

    fun withResourceName(resourceName: String) = apply { this.resourceName = resourceName }

    fun withSubstring(substring: String) = apply { this.substring = substring }

    fun withSpinnerText(spinnerText: String) = apply { this.spinnerText = spinnerText }

    fun withTag(tag: Any) = apply { this.tag = tag }

    fun withTagKey(tagKey: Int) = apply { this.tagKey = tagKey }

    fun withText(@StringRes textId: Int) = apply { this.text = stringFromResource(textId) }

    fun withText(text: String) = apply { this.text = text }

    fun startsWith(text: String) = apply { this.startsWith = text }

    fun withVisibility(visibility: ViewMatchers.Visibility) = apply { this.visibility = visibility }

    fun withCustomMatcher(matcher: Matcher<View>) = apply { this.customMatcher = matcher }

    /** [ViewInteraction] action wrappers. **/
    fun click() = apply { waitForView(viewInteraction()).perform(ViewActions.click()) }

    fun clearText() = apply {
        waitForView(viewInteraction()).perform(ViewActions.clearText(), ViewActions.closeSoftKeyboard())
    }

    fun closeDrawer() = apply { waitForView(viewInteraction()).perform(DrawerActions.close()) }

    fun closeKeyboard() = apply { waitForView(viewInteraction()).perform(ViewActions.closeSoftKeyboard()) }

    fun customAction(action: ViewAction) = apply { waitForView(viewInteraction()).perform(action) }

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

    fun checkIsChecked() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(ViewMatchers.isChecked()))
    }

    fun checkIsNotChecked() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isChecked())))
    }

    fun checkDisplayed() = apply { viewInteraction().check(ViewAssertions.matches(ViewMatchers.isDisplayed())) }

    fun checkDoesNotExist() = apply { waitUntilViewIsGone(viewInteraction()) }

    fun checkDisabled() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
    }

    fun checkEnabled() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    fun checkNotDisplayed() = apply {
        viewInteraction().check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    fun checkSelected() = apply {
        waitForView(viewInteraction()).check(ViewAssertions.matches(ViewMatchers.isSelected()))
    }

    /** [ViewInteraction] wait functions. **/
    fun wait(timeout: Long = TIMEOUT_10S) = apply { waitForView(viewInteraction(), timeout) }

    fun waitUntilGone(timeout: Long = TIMEOUT_10S) = apply { waitUntilViewIsGone(viewInteraction(), timeout) }

    fun waitForEnabled(timeout: Long = TIMEOUT_10S) = apply {
        isEnabled = true
        isDisabled = false
        waitForView(viewInteraction(), timeout)
    }

    fun waitForDisabled(timeout: Long = TIMEOUT_10S) = apply {
        isDisabled = true
        isEnabled = false
        waitForView(viewInteraction(), timeout)
    }

    /** Indicates that [View] is part of a root view described by [OnRootView]. **/
    fun inRoot(rootView: OnRootView) = apply { rootMatcher = rootView.matcher() }

    /** Builds final [Matcher] for the view. **/
    internal fun matcher(): Matcher<View> = viewMatcher()

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
        if (spinnerText != null) {
            matchers.add(ViewMatchers.withSpinnerText(spinnerText))
        }
        if (substring != null) {
            matchers.add(ViewMatchers.withSubstring(substring))
        }
        if (clazz != null) {
            matchers.add(CoreMatchers.instanceOf(clazz))
        }
        if (ancestorMatcher != null) {
            matchers.add(ViewMatchers.isDescendantOfA(ancestorMatcher))
        }
        if (tag != null) {
            matchers.add(ViewMatchers.withTagValue(CoreMatchers.`is`(tag)))
        }
        if (tagKey != null) {
            matchers.add(ViewMatchers.withTagKey(tagKey!!))
        }
        if (hint != null) {
            matchers.add(ViewMatchers.withHint(hint))
        }
        if (inputType != null) {
            matchers.add(ViewMatchers.withInputType(inputType!!))
        }
        if (visibility != null) {
            matchers.add(ViewMatchers.withEffectiveVisibility(visibility))
        }
        if (parentMatcher != null) {
            matchers.add(ViewMatchers.withParent(parentMatcher))
        }
        if (indexInParent != null) {
            matchers.add(ViewMatchers.withParentIndex(indexInParent!!))
        }
        if (className != null) {
            matchers.add(ViewMatchers.withClassName(CoreMatchers.equalTo(className)))
        }
        if (resourceName != null) {
            matchers.add(ViewMatchers.withResourceName(resourceName))
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
        if (descendantMatcher != null) {
            matchers.add(ViewMatchers.hasDescendant(descendantMatcher))
        }
        if (siblingMatcher != null) {
            matchers.add(ViewMatchers.hasSibling(siblingMatcher))
        }
        if (displayedPercentage != null) {
            matchers.add(ViewMatchers.isDisplayingAtLeast(displayedPercentage!!))
        }
        if (errorText != null) {
            matchers.add(ViewMatchers.hasErrorText(errorText))
        }
        if (childCount != null) {
            matchers.add(ViewMatchers.hasChildCount(childCount!!))
        }
        if (imeAction != null) {
            matchers.add(ViewMatchers.hasImeAction(imeAction!!))
        }
        if (supportsInputMethods) {
            matchers.add(ViewMatchers.supportsInputMethods())
        }
        if (isCompletelyDisplayed) {
            matchers.add(ViewMatchers.isCompletelyDisplayed())
        }
        if (isClickable) {
            matchers.add(ViewMatchers.isClickable())
        }
        if (isChecked) {
            matchers.add(ViewMatchers.isChecked())
        }
        if (isDisabled) {
            matchers.add(CoreMatchers.not(ViewMatchers.isEnabled()))
        }
        if (isEnabled) {
            matchers.add(ViewMatchers.isEnabled())
        }
        if (isFocusable) {
            matchers.add(ViewMatchers.isFocusable())
        }
        if (isFocused) {
            matchers.add(ViewMatchers.isFocused())
        }
        if (isNotChecked) {
            matchers.add(ViewMatchers.isNotChecked())
        }
        if (isSelected) {
            matchers.add(ViewMatchers.isSelected())
        }
        if (hasLinks) {
            matchers.add(ViewMatchers.hasLinks())
        }
        if (customMatcher != null) {
            matchers.add(customMatcher!!)
        }
        if (startsWith != null) {
            matchers.add(ViewMatchers.withText(CoreMatchers.startsWith(startsWith)))
        }
        return AllOf.allOf(matchers)
    }

    companion object {
        /** Default rootMatcher value for [OnListView] instance. **/
        private var rootMatcher: Matcher<Root> = RootMatchers.DEFAULT
    }
}
