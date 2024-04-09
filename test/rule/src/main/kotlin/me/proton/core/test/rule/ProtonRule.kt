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

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.test.rule.di.TestEnvironmentConfigModule.provideEnvironmentConfiguration
import me.proton.core.test.rule.entity.HiltConfig
import me.proton.core.test.rule.entity.TestConfig
import me.proton.core.test.rule.entity.UserConfig
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
 * @param hiltConfig Configuration for hooking into hilt setup
 */
public open class ProtonRule(
    private val userConfig: UserConfig?,
    private val testConfig: TestConfig,
    private val hiltConfig: HiltConfig?,
) : TestRule {

    public val activityScenarioRule: TestRule? = testConfig.activityRule

    private val targetContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val environmentConfigRule by lazy {
        EnvironmentConfigRule(testConfig.envConfig)
    }

    private val hiltRule: HiltAndroidRule? by lazy {
        HiltAndroidRule(hiltConfig?.hiltInstance ?: return@lazy null)
    }

    private val hiltInjectRule by lazy {
        if (hiltConfig == null) return@lazy null
        before {
            hiltRule!!.inject()
        }
    }

    public val testDataRule: QuarkTestDataRule? by lazy {
        if (userConfig?.userData == null && testConfig.annotationTestData.isEmpty()) return@lazy null
        QuarkTestDataRule(
            testConfig.annotationTestData,
            initialTestUserData = userConfig?.userData,
            environmentConfiguration = {
                provideEnvironmentConfiguration(ContentResolverConfigManager(targetContext))
            }
        )
    }

    private val authenticationRule by lazy {
        if (userConfig == null) return@lazy null.also {
            printInfo("No UserConfig provided. Skipping authentication.")
        }
        AuthenticationRule {
            UserConfig(
                testDataRule?.testUserData,
                loginBefore = userConfig.loginBefore,
                logoutBefore = userConfig.logoutBefore,
                logoutAfter = userConfig.logoutAfter,
            )
        }
    }

    private val beforeHiltRule by lazy {
        if (hiltConfig?.beforeHilt == null) return@lazy null
        before {
            printInfo("Executing beforeHilt()")
            hiltConfig!!.beforeHilt.invoke(this)
        }
    }

    private val afterHiltRule by lazy {
        if (hiltConfig?.afterHilt == null) return@lazy null
        before {
            printInfo("Executing afterHilt()")
            hiltConfig!!.afterHilt.invoke(this)
        }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return RuleChain
            .outerRule(beforeHiltRule)
            .aroundNullable(hiltRule)
            .around(environmentConfigRule)
            .around(hiltInjectRule)
            .aroundNullable(afterHiltRule)
            .aroundNullable(testDataRule)
            .aroundNullable(authenticationRule)
            .aroundNullable(testConfig.activityRule)
            .around(TestExecutionWatcher())
            .apply(base, description)
    }
}

private fun RuleChain.aroundNullable(rule: TestRule?): RuleChain {
    return around(rule ?: return this)
}

public fun <T> T.before(block: suspend T.() -> Any): ExternalResource =
    object : ExternalResource() {
        override fun before() {
            runBlocking {
                block()
            }
        }
    }

public fun Any.printInfo(message: String) {
    val (tag, msg) = this::class.java.name to "[ProtonRule] -> $message"
    if (message.contains("CRITICAL") || message.contains("failed!"))
        Log.e(tag, msg)
    else
        Log.i(tag, msg)
}
