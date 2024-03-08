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

import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.handleExternal
import me.proton.core.test.rule.annotation.toEnvironmentConfiguration
import me.proton.core.test.rule.extension.seedTestUserData
import okhttp3.OkHttpClient
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A JUnit test rule that manages test data setup and teardown using Quark commands for configuring test environments
 * and user data. This rule facilitates dynamic application of test data and environment configurations based
 * on annotations or provided initial settings, ensuring tests run with the required setup.
 *
 * @property initialTestUserData Initial user data to set up before tests. Can be overridden by test-specific
 * annotations.
 * @property environmentConfig A lambda expression that supplies the [EnvironmentConfig] used for test execution.
 */
public class QuarkTestDataRule(
    private vararg val annotationTestData: AnnotationTestData<Annotation>,
    initialTestUserData: TestUserData?,
    private val environmentConfig: () -> EnvironmentConfig
) : TestWatcher() {

    private lateinit var quarkCommand: QuarkCommand

    private val environmentConfiguration: EnvironmentConfiguration by lazy {
        environmentConfig().toEnvironmentConfiguration()
    }

    private var createdUserResponse: CreateUserQuarkResponse? = null

    private val testDataMap: MutableMap<Class<out Annotation>, Annotation> = mutableMapOf()

    /**
     * The user data applied to the current test, initially set from [initialTestUserData] and potentially
     * overridden by test-specific annotations.
     */
    public var testUserData: TestUserData? = initialTestUserData?.handleExternal()
        private set

    /**
     * Prepares and applies test data and configurations before each test starts. This includes setting up
     * user/env data and processing all test data entries against the test's annotations.
     */
    override fun starting(description: Description) {
        quarkCommand = QuarkCommand(quarkClient)
            .baseUrl("https://${environmentConfiguration.host}/api/internal")
            .proxyToken(environmentConfiguration.proxyToken)

        testUserData = description.getAnnotation(TestUserData::class.java)?.handleExternal() ?: testUserData

        testUserData?.takeIf { it.shouldSeed }?.apply {
            createdUserResponse = quarkCommand.seedTestUserData(this)
        }

        annotationTestData.forEach { entry ->
            val annotationData = description.getAnnotation(entry.annotationClass) ?: entry.default
            entry.implementation(quarkCommand, annotationData, testUserData, createdUserResponse)
            testDataMap[entry.annotationClass] = annotationData
        }
    }

    /** Clean up test data and configurations after each test finishes. **/
    override fun finished(description: Description) {
        annotationTestData.forEach { data ->
            testDataMap[data.annotationClass]?.let {
                data.tearDown?.invoke(
                    quarkCommand,
                    it,
                    testUserData,
                    createdUserResponse
                )
            }
        }
    }

    /** Retrieves test data of a specific annotation type, if applied to the current test. **/
    @Suppress("UNCHECKED_CAST")
    public fun <T : Annotation> getTestData(annotationClass: Class<T>): T = testDataMap[annotationClass] as T

    private val AnnotationTestData<Annotation>.annotationClass: Class<out Annotation>
        get() = default.annotationClass.java

    public companion object {
        public val quarkClientTimeout: AtomicReference<Duration> = AtomicReference(60.seconds)

        private val quarkClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(quarkClientTimeout.get().toJavaDuration())
                .readTimeout(quarkClientTimeout.get().toJavaDuration())
                .writeTimeout(quarkClientTimeout.get().toJavaDuration())
                .build()
        }
    }
}