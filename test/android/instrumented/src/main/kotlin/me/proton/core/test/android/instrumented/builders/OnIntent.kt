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

package me.proton.core.test.android.instrumented.builders

import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.espresso.intent.ActivityResultFunction
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import me.proton.core.test.android.instrumented.waits.ConditionWatcher.Companion.TIMEOUT_5S
import me.proton.core.test.android.instrumented.waits.IntentWaits.waitUntilIntentMatcherFulfilled
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

/**
 * Builder like class that simplifies [intending] and [intended] syntax.
 */
class OnIntent {
    private var anyIntentMatcher: Matcher<Intent>? = null
    private var isInternalMatcher: Matcher<Intent>? = null
    private var flag: Int? = null
    private var flags: Int? = null

    private var extraValue: Any? = null
    private var extraValueMatcher: Matcher<Any>? = null

    private var filterIntent: Intent? = null

    private var action: String? = null
    private var data: String? = null
    private var extraKey: String? = null
    private var extraKeyMatcher: Matcher<String>? = null
    private var extraWithKey: String? = null
    private var packageName: String? = null
    private var packageNameToPackage: String? = null
    private var toPackage: String? = null
    private var type: String? = null

    private var dataUri: Uri? = null

    private var dataUriMatcher: Matcher<Uri>? = null

    private var categories: Set<String>? = null
    private var component: String? = null
    private var componentName: ComponentName? = null

    private var componentNameMatcher: Matcher<ComponentName>? = null

    private var actionMatcher: Matcher<String>? = null
    private var dataString: Matcher<String>? = null
    private var extraWithKeyMatcher: Matcher<String>? = null
    private var packageNameMatcher: Matcher<String>? = null
    private var typeMatcher: Matcher<String>? = null

    private var extraMatcherBundle: Matcher<Bundle>? = null

    fun anyIntent() = apply { this.anyIntentMatcher = IntentMatchers.anyIntent() }

    fun isInternal() = apply { this.isInternalMatcher = IntentMatchers.isInternal() }

    fun filterEquals(filterIntent: Intent) = apply { this.filterIntent = filterIntent }

    /** See [Intent] for the list of actions. **/
    fun hasAction(action: String) = apply { this.action = action }

    fun hasAction(actionMatcher: Matcher<String>) = apply { this.actionMatcher = actionMatcher }

    fun hasCategories(categories: Set<String>) = apply { this.categories = categories }

    fun hasComponent(component: String) = apply { this.component = component }

    fun hasComponent(componentName: ComponentName) = apply { this.componentName = componentName }

    fun hasComponent(componentNameMatcher: Matcher<ComponentName>) =
        apply { this.componentNameMatcher = componentNameMatcher }

    fun hasData(data: String) = apply { this.data = data }

    fun hasDataUri(dataUri: Uri) = apply { this.dataUri = dataUri }

    fun hasDataUriMatcher(dataUriMatcher: Matcher<Uri>) = apply { this.dataUriMatcher = dataUriMatcher }

    fun hasDataString(dataString: Matcher<String>) = apply { this.dataString = dataString }

    fun hasExtra(extraKey: String, extraValue: Any) = apply {
        this.extraKey = extraKey
        this.extraValue = extraValue
    }

    fun hasExtra(extraKeyMatcher: Matcher<String>, extraValueMatcher: Matcher<Any>) = apply {
        this.extraKeyMatcher = extraKeyMatcher
        this.extraValueMatcher = extraValueMatcher
    }

    fun hasExtras(extraMatcherBundle: Matcher<Bundle>) = apply { this.extraMatcherBundle = extraMatcherBundle }

    fun hasExtraWithKey(extraWithKey: String) = apply { this.extraWithKey = extraWithKey }

    fun hasExtraWithKey(extraWithKeyMatcher: Matcher<String>) = apply { this.extraWithKeyMatcher = extraWithKeyMatcher }

    fun hasFlag(flag: Int) = apply { this.flag = flag }

    fun hasFlags(flags: Int) = apply { this.flags = flags }

    fun hasPackage(packageName: String) = apply { this.packageName = packageName }

    fun hasPackage(packageNameMatcher: Matcher<String>) = apply { this.packageNameMatcher = packageNameMatcher }

    fun hasType(type: String) = apply { this.type = type }

    fun hasType(typeMatcher: Matcher<String>) = apply { this.typeMatcher = typeMatcher }

    fun toPackage(toPackage: String) = apply { this.toPackage = toPackage }

    // Checks with wait that intent with given matchers is sent
    fun checkSent(timeout: Long = TIMEOUT_5S) {
        waitUntilIntentMatcherFulfilled(intentMatcher(), timeout)
    }

    fun respondWith(result: Instrumentation.ActivityResult) {
        intending(intentMatcher()).respondWith(result)
    }

    fun respondWithFunction(resultFunction: ActivityResultFunction) {
        intending(intentMatcher()).respondWithFunction(resultFunction)
    }

    private fun intentMatcher(): Matcher<Intent> {
        val matchers = mutableListOf(
            anyIntentMatcher,
            isInternalMatcher
        )

        if (filterIntent != null) {
            matchers.add(IntentMatchers.filterEquals(filterIntent))
        }
        if (action != null) {
            matchers.add(IntentMatchers.hasAction(action))
        }
        if (actionMatcher != null) {
            matchers.add(IntentMatchers.hasAction(actionMatcher))
        }
        if (categories != null) {
            matchers.add(IntentMatchers.hasCategories(categories))
        }
        if (component != null) {
            matchers.add(IntentMatchers.hasComponent(component))
        }
        if (componentName != null) {
            matchers.add(IntentMatchers.hasComponent(componentName))
        }
        if (componentNameMatcher != null) {
            matchers.add(IntentMatchers.hasComponent(componentNameMatcher))
        }
        if (data != null) {
            matchers.add(IntentMatchers.hasData(data))
        }
        if (dataUri != null) {
            matchers.add(IntentMatchers.hasData(dataUri))
        }
        if (dataUriMatcher != null) {
            matchers.add(IntentMatchers.hasData(dataUriMatcher))
        }
        if (dataString != null) {
            matchers.add(IntentMatchers.hasDataString(dataString))
        }
        if (extraKey != null && extraValue != null) {
            matchers.add(IntentMatchers.hasExtra(extraKey, extraValue))
        }
        if (extraKeyMatcher != null && extraValueMatcher != null) {
            matchers.add(IntentMatchers.hasExtra(extraKeyMatcher, extraValueMatcher))
        }
        if (extraMatcherBundle != null) {
            matchers.add(IntentMatchers.hasExtras(extraMatcherBundle))
        }
        if (extraWithKey != null) {
            matchers.add(IntentMatchers.hasExtraWithKey(extraWithKey))
        }
        if (extraWithKeyMatcher != null) {
            matchers.add(IntentMatchers.hasExtraWithKey(extraWithKeyMatcher))
        }
        if (filterIntent != null) {
            matchers.add(IntentMatchers.filterEquals(filterIntent))
        }
        if (flag != null) {
            matchers.add(IntentMatchers.hasFlag(flag!!))
        }
        if (flags != null) {
            matchers.add(IntentMatchers.hasFlags(flags!!))
        }
        if (type != null) {
            matchers.add(IntentMatchers.hasType(type))
        }
        if (typeMatcher != null) {
            matchers.add(IntentMatchers.hasType(typeMatcher))
        }
        if (packageName != null) {
            matchers.add(IntentMatchers.hasPackage(packageName))
        }
        if (packageNameMatcher != null) {
            matchers.add(IntentMatchers.hasPackage(packageNameMatcher))
        }
        if (packageNameToPackage != null) {
            matchers.add(IntentMatchers.toPackage(packageNameToPackage))
        }
        return AllOf.allOf(matchers.filterNotNull())
    }
}
