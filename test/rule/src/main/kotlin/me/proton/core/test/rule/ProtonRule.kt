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

package me.proton.core.test.rule

import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.di.TestEnvironmentConfigModule.provideEnvironmentConfiguration
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Base class for custom test rules in the Proton framework.
 *
 * Provides a structured approach to managing various aspects of test setup and execution:
 *
 * - **Environment configuration:** Manages environment configuration using `EnvironmentConfigRule`.
 * - **Hilt dependencies:** Handles Hilt dependency injection using `HiltAndroidRule`.
 * - **Test data:** Provides test data management through `QuarkTestDataRule`.
 * - **User authentication:** Optionally enables user authentication with `AuthenticationRule`
 *                            based on provided `TestUserData`.
 * - **User-defined setup:** Allows executing custom setup logic before each test through a provided lambda function.
 *
 * @param userConfig Configuration for user data and related behavior (e.g., login/logout settings).
 * @param testConfig Configuration for environment, test data, and an optional activity rule.
 * @param hiltTestInstance Hilt test instance for dependency injection.
 * @param setup A lambda function containing setup logic to be executed before each test.
 */
public open class ProtonRule(
    private val userConfig: UserConfig,
    private val testConfig: TestConfig,
    private val hiltTestInstance: Any,
    private val setup: () -> Any
) : TestRule {

    private val environmentConfigRule by lazy {
        val envConfig = testConfig.envConfig ?: EnvironmentConfig.fromConfiguration(provideEnvironmentConfiguration())
        EnvironmentConfigRule(envConfig)
    }

    private val hiltRule by lazy {
        HiltAndroidRule(hiltTestInstance)
    }

    public val testDataRule: QuarkTestDataRule by lazy {
        QuarkTestDataRule(
            *testConfig.annotationTestData,
            initialTestUserData = userConfig.userData,
            environmentConfig = { environmentConfigRule.config }
        )
    }

    private val authenticationRule by lazy {
        userConfig
            .takeUnless { it.userData == null }
            ?.let {
                AuthenticationRule(it)
            }
    }

    private val setupRule by lazy {
        before { setup() }
    }

    private val ruleChain: RuleChain by lazy {
        RuleChain.outerRule(environmentConfigRule)
            .around(hiltRule)
            .around(setupRule)
            .around(testDataRule)
            .aroundNullable(authenticationRule)
            .aroundNullable(testConfig.activityRule)
    }

    override fun apply(base: Statement, description: Description): Statement = ruleChain.apply(base, description)

    public data class UserConfig(
        val userData: TestUserData? = null,
        val loginBefore: Boolean = true,
        val logoutBefore: Boolean = true,
        val logoutAfter: Boolean = true
    ) {
        val overrideLogin: Boolean get() = loginBefore || logoutBefore || logoutAfter
    }

    public data class TestConfig(
        val envConfig: EnvironmentConfig? = null,
        val annotationTestData: Array<out AnnotationTestData<Annotation>> = emptyArray(),
        val activityRule: TestRule? = null,
    )

    private fun RuleChain.aroundNullable(rule: TestRule?): RuleChain {
        return around(rule ?: return this)
    }
}

public fun <T> T.before(block: suspend T.() -> Any): ExternalResource =
    object : ExternalResource() {
        override fun before() {
            runBlocking {
                block()
            }
        }
    }
