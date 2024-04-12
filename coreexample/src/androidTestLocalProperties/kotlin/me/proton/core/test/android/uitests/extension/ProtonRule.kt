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

package me.proton.core.test.android.uitests.extension

import androidx.test.ext.junit.rules.activityScenarioRule
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.extension.protonActivityScenarioRule

fun Any.coreExampleRule(
    annotationTestData: Set<AnnotationTestData<Annotation>> = setOf(),
    envConfig: EnvironmentConfig? = null,
    userData: TestUserData? = TestUserData(TestUserData.randomUsername()),
    loginBefore: Boolean = true,
    logoutBefore: Boolean = true,
    logoutAfter: Boolean = true,
    setUp: (ProtonRule) -> Unit = { },
): ProtonRule = protonActivityScenarioRule<MainActivity>(
    annotationTestData,
    envConfig = envConfig,
    userData = userData,
    loginBefore = loginBefore,
    logoutBefore = logoutBefore,
    logoutAfter = logoutAfter,
    activityScenarioRule = activityScenarioRule<MainActivity>(),
    afterHilt = setUp
)