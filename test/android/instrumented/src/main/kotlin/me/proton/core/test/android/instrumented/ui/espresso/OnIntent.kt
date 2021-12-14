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

import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.espresso.intent.ActivityResultFunction
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import me.proton.core.test.android.instrumented.ConditionWatcher
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

/**
 * Builder like class that simplifies [intending] and [intended] syntax.
 */
class OnIntent : ConditionWatcher {
    val matchers = mutableListOf(
        IntentMatchers.anyIntent(),
        IntentMatchers.isInternal()
    )

    fun anyIntent() = apply {
        matchers.add(IntentMatchers.anyIntent())
    }

    fun isInternal() = apply {
        matchers.add(IntentMatchers.isInternal())
    }

    fun filterEquals(filterIntent: Intent) = apply {
        matchers.add(IntentMatchers.filterEquals(filterIntent))
    }

    /** See [Intent] for the list of actions. **/
    fun hasAction(action: String) = apply {
        matchers.add(IntentMatchers.hasAction(action))
    }

    fun hasAction(actionMatcher: Matcher<String>) = apply {
        matchers.add(IntentMatchers.hasAction(actionMatcher))
    }

    fun hasCategories(categories: Set<String>) = apply {
        matchers.add(IntentMatchers.hasCategories(categories))
    }

    fun hasComponent(component: String) = apply {
        this.matchers.add(IntentMatchers.hasComponent(component))
    }

    fun hasComponent(componentName: ComponentName) = apply {
        matchers.add(IntentMatchers.hasComponent(componentName))
    }

    fun hasComponent(componentNameMatcher: Matcher<ComponentName>) = apply {
        matchers.add(IntentMatchers.hasComponent(componentNameMatcher))
    }

    fun hasData(data: String) = apply {
        matchers.add(IntentMatchers.hasData(data))
    }

    fun hasDataUri(dataUri: Uri) = apply {
        matchers.add(IntentMatchers.hasData(dataUri))
    }

    fun hasDataUriMatcher(dataUriMatcher: Matcher<Uri>) = apply {
        matchers.add(IntentMatchers.hasData(dataUriMatcher))
    }

    fun hasDataString(dataString: Matcher<String>) = apply {
        matchers.add(IntentMatchers.hasDataString(dataString))
    }

    fun hasExtra(extraKey: String, extraValue: Any) = apply {
        matchers.add(IntentMatchers.hasExtra(extraKey, extraValue))
    }

    fun hasExtra(extraKeyMatcher: Matcher<String>, extraValueMatcher: Matcher<Any>) = apply {
        matchers.add(IntentMatchers.hasExtra(extraKeyMatcher, extraValueMatcher))
    }

    fun hasExtras(extraMatcherBundle: Matcher<Bundle>) = apply {
        matchers.add(IntentMatchers.hasExtras(extraMatcherBundle))
    }

    fun hasExtraWithKey(extraWithKey: String) = apply {
        matchers.add(IntentMatchers.hasExtraWithKey(extraWithKey))
    }

    fun hasExtraWithKey(extraWithKeyMatcher: Matcher<String>) = apply {
        matchers.add(IntentMatchers.hasExtraWithKey(extraWithKeyMatcher))
    }

    fun hasFlag(flag: Int) = apply {
        matchers.add(IntentMatchers.hasFlag(flag))
    }

    fun hasFlags(flags: Int) = apply {
        matchers.add(IntentMatchers.hasFlags(flags))
    }

    fun hasPackage(packageName: String) = apply {
        matchers.add(IntentMatchers.hasPackage(packageName))
    }

    fun hasPackage(packageNameMatcher: Matcher<String>) = apply {
        matchers.add(IntentMatchers.hasPackage(packageNameMatcher))
    }

    fun hasType(type: String) = apply {
        matchers.add(IntentMatchers.hasType(type))
    }

    fun hasType(typeMatcher: Matcher<String>) = apply {
        matchers.add(IntentMatchers.hasType(typeMatcher))
    }

    fun toPackage(toPackage: String) = apply {
        matchers.add(IntentMatchers.toPackage(toPackage))
    }

    // Checks with wait that intent with given matchers is sent
    fun checkSent() {
        waitForCondition({ intended(intentMatcher()) })
    }

    fun respondWith(result: Instrumentation.ActivityResult) {
        intending(intentMatcher()).respondWith(result)
    }

    fun respondWithFunction(resultFunction: ActivityResultFunction) {
        intending(intentMatcher()).respondWithFunction(resultFunction)
    }

    private fun intentMatcher() = AllOf.allOf(matchers)
}
