/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.test.rule.extension

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.entity.HiltConfig
import me.proton.core.test.rule.entity.TestConfig
import me.proton.core.test.rule.entity.UserConfig
import me.proton.test.fusion.FusionConfig
import org.junit.rules.TestRule

/**
 * Creates a `ProtonRule` instance for managing test setup and execution.
 *
 * This function offers fine-grained control over various test aspects, including:
 *  - Environment configuration
 *  - Custom setup logic
 *  - Activity or Compose test rule
 *
 * @param annotationTestData Set of `AnnotationTestData` for `QuarkTestDataRule`.
 * @param envConfig Environment configuration for the test (optional).
 * @param logoutBefore Whether to perform logout before the test (default: false).
 * @param logoutAfter Whether to perform logout after the test (default: false).
 * @param activityRule Optional `TestRule` for managing activities (default: null).
 * @param afterHilt A lambda function containing setup logic to be executed before each test (default: empty).
 * @return A new `ProtonRule` instance.
 */
@SuppressWarnings("LongParameterList")
public fun Any.protonRule(
    annotationTestData: Set<AnnotationTestData<Annotation>> = emptySet(),
    envConfig: EnvironmentConfig? = null,
    logoutBefore: Boolean = false,
    logoutAfter: Boolean = false,
    activityRule: TestRule? = null,
    additionalRules: LinkedHashSet<TestRule> = linkedSetOf(),
    afterHilt: (ProtonRule) -> Any = { },
    beforeHilt: (ProtonRule) -> Any = { },
): ProtonRule {
    val userConfig = UserConfig(
        logoutBefore = logoutBefore,
        logoutAfter = logoutAfter
    )

    val testConfig = TestConfig(
        envConfig = envConfig,
        annotationTestData = annotationTestData,
        activityRule = activityRule
    )

    val hiltConfig = HiltConfig(
        hiltInstance = this,
        afterHilt = afterHilt,
        beforeHilt = beforeHilt
    )

    return ProtonRule(
        userConfig = userConfig,
        testConfig = testConfig,
        hiltConfig = hiltConfig,
        additionalRules = additionalRules
    )
}

/**
 * Creates a `ProtonRule` instance with an `ActivityScenarioRule` for a specific activity type.
 *
 * This function inherits all features of `protonRule` and uses the provided `ActivityScenarioRule`
 * for managing the activity lifecycle within the test.
 *
 * @param A The type of activity to be used with the `ActivityScenarioRule`.
 * @param annotationTestData Array of `AnnotationTestData` for `QuarkTestDataRule`.
 * @param envConfig Environment configuration for the test (optional).
 * @param logoutBefore Whether to perform logout before the test (default: true).
 * @param logoutAfter Whether to perform logout after the test (default: true).
 * @param activityScenarioRule An `ActivityScenarioRule` for the specified activity type
 *        (default: created using `activityScenarioRule()`).
 * @return A new `ProtonRule` instance.
 */
@SuppressWarnings("LongParameterList")
public inline fun <reified A : Activity> Any.protonActivityScenarioRule(
    annotationTestData: Set<AnnotationTestData<Annotation>> = emptySet(),
    envConfig: EnvironmentConfig? = null,
    logoutBefore: Boolean = true,
    logoutAfter: Boolean = false,
    activityScenarioRule: ActivityScenarioRule<A> = activityScenarioRule<A>(),
    additionalRules: LinkedHashSet<TestRule> = linkedSetOf(),
    noinline beforeHilt: (ProtonRule) -> Unit = { },
    noinline afterHilt: (ProtonRule) -> Unit = { },
): ProtonRule = protonRule(
    annotationTestData = annotationTestData,
    envConfig = envConfig,
    logoutBefore = logoutBefore,
    logoutAfter = logoutAfter,
    activityRule = activityScenarioRule,
    additionalRules = additionalRules,
    afterHilt = afterHilt,
    beforeHilt = beforeHilt
)

/**
 * Creates a `ProtonRule` instance with a `ComposeTestRule` for an Android Compose component activity.
 *
 * This function inherits all features of `protonRule` and uses the provided `ComposeTestRule`
 * for managing the Compose UI during the test.
 *
 * @param A The type of component activity to be used with the `ComposeTestRule`.
 * @param annotationTestData Array of `AnnotationTestData` for `QuarkTestDataRule`.
 * @param envConfig Environment configuration for the test (optional).
 * @param logoutBefore Whether to perform logout before the test (default: true).
 * @param logoutAfter Whether to perform logout after the test (default: true).
 * @param composeTestRule A `ComposeTestRule` for the specified component activity type
 *        (default: created using `createAndroidComposeRule()`).
 * @param afterHilt A lambda function containing setup logic to be executed before each test (default: empty).
 * @return A new `ProtonRule` instance.
 */
@SuppressWarnings("LongParameterList")
public inline fun <reified A : ComponentActivity> Any.protonAndroidComposeRule(
    annotationTestData: Set<AnnotationTestData<Annotation>> = emptySet(),
    envConfig: EnvironmentConfig? = null,
    logoutBefore: Boolean = true,
    logoutAfter: Boolean = false,
    fusionEnabled: Boolean = false,
    composeTestRule: ComposeTestRule = createAndroidComposeRule<A>(),
    additionalRules: LinkedHashSet<TestRule> = linkedSetOf(),
    noinline beforeHilt: (ProtonRule) -> Any = { },
    noinline afterHilt: (ProtonRule) -> Any = { },
): ProtonRule = protonRule(
    annotationTestData = annotationTestData,
    envConfig = envConfig,
    logoutBefore = logoutBefore,
    logoutAfter = logoutAfter,
    activityRule = composeTestRule,
    additionalRules = additionalRules,
    beforeHilt = beforeHilt,
    afterHilt = afterHilt
).apply {
    if (fusionEnabled) FusionConfig.Compose.testRule.set(activityScenarioRule as ComposeTestRule)
}