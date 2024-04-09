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
import me.proton.core.test.rule.annotation.annotationTestData
import me.proton.core.test.rule.annotation.handleExternal
import me.proton.core.test.rule.annotation.shouldHandleExternal
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

/**
 * A JUnit test rule that manages test data setup and teardown using Quark commands for configuring test environments
 * and user data. This rule facilitates dynamic application of test data and environment configurations based
 * on annotations or provided initial settings, ensuring tests run with the required setup.
 *
 * @property initialTestUserData Initial user data to set up before tests. Can be overridden by test-specific
 * annotations.
 * @property environmentConfiguration A lambda expression that supplies the [EnvironmentConfig] used for test execution.
 */
public class QuarkTestDataRule(
    private val annotationTestData: Set<AnnotationTestData<Annotation>>,
    private val initialTestUserData: TestUserData?,
    private val environmentConfiguration: () -> EnvironmentConfiguration
) : TestWatcher() {

    private lateinit var quarkCommand: QuarkCommand

    private var createdUserResponse: CreateUserQuarkResponse? = null

    private val testDataMap: MutableMap<Class<out Annotation>, Annotation> = mutableMapOf()

    /**
     * The user data applied to the current test, initially set from [initialTestUserData] and potentially
     * overridden by test-specific annotations.
     */
    public var testUserData: TestUserData? = null

    /**
     * Prepares and applies test data and configurations before each test starts. This includes setting up
     * user/env data and processing all test data entries against the test's annotations.
     */
    override fun starting(description: Description) {
        quarkCommand = getQuarkCommand(environmentConfiguration())

        if (initialTestUserData != null) {
            val userData = description.getAnnotation(TestUserData::class.java) ?: initialTestUserData
            val handledUserData = userData.handleExternal()

            if (userData.shouldHandleExternal) {
                printInfo("isExternal set to true, but external email is empty. Overriding values with name: ${handledUserData.name}, externalEmail: ${handledUserData.externalEmail}")
            }

            handledUserData.annotationTestData.let {
                val userSeedingTime = measureTime {
                    createdUserResponse = it.implementation(
                        quarkCommand,
                        it.instance,
                        null,
                        null
                    ) as CreateUserQuarkResponse
                }

                printInfo("${TestUserData::class.java.simpleName} seeding done: $createdUserResponse")
                printInfo("${TestUserData::class.java.simpleName} seeded in ${userSeedingTime.inWholeSeconds} seconds")

                testUserData = it.instance
                testDataMap[it.annotationClass] = it.instance

                printInfo("Running Test with $testUserData")
            }
        }

        annotationTestData.forEach { entry ->
            val annotationData = getRuntimeAnnotationData(description, entry)

            val seedingTime = measureTime {
                val result = annotationData.implementation(
                    quarkCommand,
                    entry.instance,
                    testUserData,
                    createdUserResponse
                )

                if (result is Response) {
                    printInfo("Seeding response received: { ${result.message} }")
                }

                printInfo("Running Test with ${entry.instance}")
            }

            printInfo("${entry.annotationClass.simpleName} data seeded in ${seedingTime.inWholeSeconds} seconds")

            testDataMap[entry.annotationClass] = annotationData.instance
        }
    }

    private fun getQuarkCommand(envConfig: EnvironmentConfiguration): QuarkCommand =
        QuarkCommand(quarkClient)
            .baseUrl("https://${envConfig.host}/api/internal")
            .proxyToken(envConfig.proxyToken)

    private inline fun <reified T : Annotation> getRuntimeAnnotationData(
        description: Description,
        defaultData: AnnotationTestData<T>
    ): AnnotationTestData<T> = AnnotationTestData(
        description.getAnnotation(T::class.java) ?: defaultData.instance,
        defaultData.implementation,
        defaultData.tearDown
    )

    /** Clean up test data and configurations after each test finishes. **/
    override fun finished(description: Description) {
        annotationTestData.forEach { entry ->
            testDataMap[entry.annotationClass]?.let {
                val result = entry.tearDown?.invoke(
                    quarkCommand,
                    it,
                    testUserData,
                    createdUserResponse
                ) ?: return@let

                if (result is Response) {
                    printInfo("Tear down response received: { ${result.message} }")
                }
            }
        }
    }

    /** Retrieves test data of a specific annotation type, if applied to the current test. **/
    @Suppress("UNCHECKED_CAST")
    public fun <T : Annotation> getTestData(annotationClass: Class<T>): T = testDataMap[annotationClass] as T

    private val AnnotationTestData<Annotation>.annotationClass: Class<out Annotation>
        get() = instance.annotationClass.java

    public companion object {
        private val quarkClientTimeout: AtomicReference<Duration> = AtomicReference(60.seconds)

        public val quarkClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(quarkClientTimeout.get().toJavaDuration())
                .readTimeout(quarkClientTimeout.get().toJavaDuration())
                .writeTimeout(quarkClientTimeout.get().toJavaDuration())
                .build()
        }
    }
}
