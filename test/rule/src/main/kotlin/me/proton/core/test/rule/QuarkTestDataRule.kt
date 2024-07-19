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

import android.annotation.SuppressLint
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.auth.presentation.testing.ProtonTestEntryPoint
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.rule.annotation.AnnotationTestData
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.MAIN_USER_TAG
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.annotationTestData
import me.proton.core.test.rule.annotation.copyWithLoginFlag
import me.proton.core.test.rule.annotation.copyWithQuarkResponseData
import me.proton.core.test.rule.annotation.copyWithSubscriptionDetails
import me.proton.core.test.rule.annotation.handleUserData
import me.proton.core.test.rule.annotation.payments.TestPaymentMethods
import me.proton.core.test.rule.annotation.payments.annotationTestData
import me.proton.core.test.rule.annotation.payments.isDefault
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

/**
 * A JUnit test rule that manages test data setup and teardown using Quark commands for configuring test environments
 * and user data. This rule facilitates dynamic application of test data and environment configurations based
 * on annotations or provided initial settings, ensuring tests run with the required setup.
 *
 * @property protonRule ProtonRule instance.
 * @property environmentConfiguration A lambda expression that supplies the [EnvironmentConfig] used for test execution.
 */
@SuppressLint("RestrictedApi")
@HiltAndroidTest
public class QuarkTestDataRule(
    private val protonRule: ProtonRule,
    public val environmentConfiguration: () -> EnvironmentConfiguration
) : TestWatcher() {

    public lateinit var quarkCommand: QuarkCommand
        private set

    private val annotationTestDataMap: MutableMap<Class<out Annotation>, Annotation> =
        mutableMapOf()
    private var prepareUserAnnotations: List<PrepareUser> = listOf()
    private var paymentMethodAnnotation: TestPaymentMethods? = null

    private val usersAnnotationTestDataList: ArrayList<AnnotationTestData<Annotation>> =
        arrayListOf()
    private var testUserAnnotationSeedDataMap: HashMap<PrepareUser, Pair<ArrayList<AnnotationTestData<Annotation>>, CreateUserQuarkResponse>> =
        hashMapOf()
    public val testDataMap: MutableMap<Class<out Annotation>, Annotation> = mutableMapOf()
    public var preparedUsers: HashMap<String, TestUserData> = hashMapOf()
        internal set
    public var mainTestUser: TestUserData? = null
        internal set
    private val usedTags = mutableSetOf<String>()

    /**
     * Prepares and applies test data and configurations before each test starts. This includes setting up
     * user/env data and processing all test data entries against the test's annotations.
     */
    override fun starting(description: Description) {
        runBlocking {
            printInfo("Starting ${QuarkTestDataRule::class.java.simpleName}")
            val method = description.testClass.kotlin.members
                .firstOrNull {
                    it.name == description.methodName
                            // Handle Parametrized test cases.
                            || description.methodName.contains("${it.name}[")
                }

            prepareUserAnnotations = method?.findAnnotations<PrepareUser>().orEmpty()
            paymentMethodAnnotation = method?.findAnnotation<TestPaymentMethods>()

            if (prepareUserAnnotations.isNotEmpty()) {
                prepareUserAnnotations.forEach { prepareUser ->

                    // Guard to have unique "withTag" values.
                    if (!usedTags.add(prepareUser.withTag)) {
                        throw IllegalArgumentException(
                            "Duplicate withTag '${prepareUser.withTag}' " +
                                    "found in PrepareUser annotations. The \"withTag\" must be unique."
                        )
                    }

                    val hasEmailOrName = prepareUser.userData.email.isNotEmpty() ||
                            prepareUser.userData.name.isNotEmpty()
                    val hasPassword = prepareUser.userData.password.isNotEmpty()

                    if (hasEmailOrName && hasPassword) {
                        /**
                         * User email and password was given in annotation this is a request to
                         * login with existing user, so ignore seeding
                         */
                        if (mainTestUser == null && prepareUser.withTag == MAIN_USER_TAG) {
                            mainTestUser =
                                prepareUser.userData.copyWithLoginFlag(prepareUser.loginBefore)
                        }
                        // Add user data to seededUsers list.
                        preparedUsers[prepareUser.withTag] = prepareUser.userData
                    } else {
                        /**
                         * Since @PrepareUser annotation as well as @TestUserData annotation have
                         * limitations as they allow only compile time constants we need to handle
                         * TestUserData object to provide required fields like email, name, etc. at
                         * run time. This is done in handleUserData().
                         */
                        var handledUserData = prepareUser.userData.handleUserData()
                        var createdUserResponse: CreateUserQuarkResponse?
                        quarkCommand = getQuarkCommand(environmentConfiguration())

                        val userSeedingTime = measureTime {
                            createdUserResponse = prepareUser.annotationTestData.implementation(
                                quarkCommand,
                                prepareUser.annotationTestData.instance,
                                null,
                                handledUserData,
                                mapOf()
                            ) as CreateUserQuarkResponse
                        }
                        usersAnnotationTestDataList.add(prepareUser.annotationTestData)

                        // Add seeded user to User map for later retrieval by title and usage in tests.
                        createdUserResponse?.let {
                            /**
                             * Update @TestUserData object with data that came from seeding response.
                             */
                            handledUserData = handledUserData.copyWithQuarkResponseData(
                                createdUserResponse!!,
                                prepareUser.loginBefore
                            )
                            if (mainTestUser == null && prepareUser.withTag == MAIN_USER_TAG) {
                                mainTestUser = handledUserData
                            }
                        } ?: error("Could not seed the user.")

                        printInfo("${PrepareUser::class.java.simpleName} seeding done: $createdUserResponse")
                        printInfo(
                            "${PrepareUser::class.java.simpleName} seeded User: " +
                                    "${prepareUser.userData.name} in ${userSeedingTime.inWholeSeconds} seconds"
                        )

                        if (!prepareUser.subscriptionData.isDefault()) {
                            prepareUser.subscriptionData.annotationTestData.implementation.invoke(
                                quarkCommand,
                                prepareUser.subscriptionData.annotationTestData.instance,
                                createdUserResponse,
                                null,
                                mapOf()
                            )

                            handledUserData =
                                handledUserData.copyWithSubscriptionDetails(prepareUser.subscriptionData)
                        }

                        // Finally add handledUserData to seededUsers list.
                        preparedUsers[prepareUser.withTag] = handledUserData

                        /**
                         * Get Subscription AnnotationTestData instance and save it in
                         * usersAnnotationTestDataList.
                         */
                        usersAnnotationTestDataList.add(prepareUser.subscriptionData.annotationTestData)


                        /**
                         * Map usersAnnotationTestDataList to TestUser instance for future use
                         * in finished().
                         */
                        testUserAnnotationSeedDataMap[prepareUser] =
                            Pair(usersAnnotationTestDataList, createdUserResponse!!)
                    }
                }
                printInfo("Running test with ${mainTestUser}.")
            } else {
                printInfo(
                    "No users to seed. Running \"${protonRule.testName}\" test " +
                            "without seeding."
                )
            }

            /**
             * Check for PaymentMethodAnnotation presence and invoke its implementation when exist.
             */
            if (paymentMethodAnnotation != null) {
                paymentMethodAnnotation!!.annotationTestData.implementation.invoke(
                    quarkCommand,
                    paymentMethodAnnotation!!.annotationTestData.instance,
                    null,
                    null,
                    null
                )
            }

            /**
             * Iterate over all AnnotationTestData instances from ProtonRule.
             * This AnnotationTestData receives seededUsers map which allows linking seeded users
             * with needed actions using users tags.
             */
            protonRule.testConfig.annotationTestData.forEach { entry ->
                val seedingTime = measureTime {
                    val result = entry.implementation(
                        quarkCommand,
                        entry.instance,
                        null,
                        null,
                        preparedUsers
                    )

                    if (result is Response) {
                        printInfo("Seeding response received: { ${result.message} }")
                    }
                }
                printInfo("${entry.annotationClass.simpleName} data seeded in ${seedingTime.inWholeSeconds} seconds")
                testDataMap[entry.annotationClass] = entry.instance
            }
            printInfo("Done with starting ${this::class.java.simpleName}")
        }
    }

    /** Clean up test data and configurations after each test finishes. **/
    override fun finished(description: Description) {

        /**
         * Passing seededUsers mapOf<String, TestUserData> to annotationTestData.tearDown
         * allows us to link annotation to registered user data (i.e. TestUserData) which
         * at this stage is seeded and has needed data to perform proper clean up.
         */
        prepareUserAnnotations.forEach { testUser ->
            usedTags.clear()
            testUserAnnotationSeedDataMap[testUser]?.let { (testUserAnnotationDataList, createdUserResponse) ->
                testUserAnnotationDataList.forEach { annotation ->
                    annotation.tearDown?.invoke(
                        quarkCommand,
                        annotation.instance,
                        createdUserResponse,
                        null,
                        preparedUsers
                    )?.let { result ->
                        if (result is Response) {
                            printInfo("Tear down response received: { ${result.message} }")
                        }
                    }
                }
            }
        }

        /**
         * Iterate over all AnnotationTestData instances from ProtonRule to trigger tearDown.
         * This AnnotationTestData shouldn't be focused on user but on test environment.
         * Therefore user data is not passed in implementation.
         */
        protonRule.testConfig.annotationTestData.forEach { entry ->
            annotationTestDataMap[entry.annotationClass]?.let { annotationData ->
                entry.tearDown?.invoke(
                    quarkCommand,
                    annotationData,
                    null,
                    null,
                    null
                )?.let { result ->
                    if (result is Response) {
                        printInfo("Tear down response received: { ${result.message} }")
                    }
                }
            }
        }
    }

    private fun getQuarkCommand(envConfig: EnvironmentConfiguration): QuarkCommand =
        QuarkCommand(quarkClient)
            .baseUrl("https://${envConfig.host}/api/internal")
            .proxyToken(envConfig.proxyToken)

    public fun getAnnotationProperty(annotation: Annotation, propertyName: String): Any? {
        return try {
            // Use reflection to access the property of the annotation by name
            val method = annotation.javaClass.getMethod(propertyName)
            method.invoke(annotation)
        } catch (e: Exception) {
            null
        }
    }

    private val AnnotationTestData<Annotation>.annotationClass: Class<out Annotation>
        get() = instance.annotationClass.java

    public companion object {

        private val protonTestEntryPoint by lazy {
            EntryPointAccessors.fromApplication(
                ApplicationProvider.getApplicationContext<Application>(),
                ProtonTestEntryPoint::class.java
            )
        }

        public val authHelper: LoginTestHelper by lazy { protonTestEntryPoint.loginTestHelper }

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