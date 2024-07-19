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

package me.proton.core.test.rule.annotation

import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.v2.QuarkCommand

/**
 * Represents a test data configuration for the `QuarkTestDataRule`.
 *
 * @param T The annotation type associated with this test data configuration.
 * @param instance The default annotation instance to use if no test-specific annotation is present.
 * @param implementation A lambda function defining how to apply the test data using the provided Quark command,
 *        test user data (if available), and created user response (if available).
 * @param tearDown An optional lambda function defining how to tear down the test data after the test, using the
 *        provided Quark command, test data annotation, test user data (if available),
 *        and created user response (if available).
 */
public class AnnotationTestData<out T : Annotation>(
    public val instance: T,
    public val implementation: QuarkCommand.(@UnsafeVariance T, CreateUserQuarkResponse?, TestUserData?, Map<String, TestUserData>?) -> Any,
    public val tearDown: (QuarkCommand.(@UnsafeVariance T, CreateUserQuarkResponse?, TestUserData?, Map<String, TestUserData>?) -> Any?)? = null,
) {
    public companion object {
        // Factory method for default implementation.
        public fun <T : Annotation> forDefault(
            default: T,
            implementation: QuarkCommand.(T) -> Any,
            tearDown: (QuarkCommand.(T) -> Any)? = null
        ): AnnotationTestData<T> {
            return AnnotationTestData(
                instance = default,
                implementation = { data, _, _, _ -> implementation(data) },
                tearDown = { data, _, _, _ -> tearDown?.invoke(this, data) }
            )
        }

        // Factory method for CreateUserQuarkResponse.
        public fun <T : Annotation> forCreateUserQuarkResponse(
            default: T,
            implementation: QuarkCommand.(T, CreateUserQuarkResponse) -> Any,
            tearDown: (QuarkCommand.(T, CreateUserQuarkResponse) -> Unit)? = null
        ): AnnotationTestData<T> {
            return AnnotationTestData(
                instance = default,
                implementation = { data, seededUser, _, _ ->
                    implementation(
                        data,
                        seededUser ?: error("Cannot seed test data - TestUserData is not provided.")
                    )
                },
                tearDown = { data, seededUser, _, _ ->
                    tearDown?.invoke(
                        this,
                        data,
                        seededUser
                            ?: error("Cannot tear down test data - TestUserData is not provided.")
                    )
                }
            )
        }

        // Factory method for TestUserData.
        public fun <T : Annotation> forTestUserData(
            default: T,
            implementation: QuarkCommand.(T, TestUserData) -> Any,
            tearDown: (QuarkCommand.(T, CreateUserQuarkResponse?) -> Unit)? = null
        ): AnnotationTestData<T> {
            return AnnotationTestData(
                instance = default,
                implementation = { data, _, userData, _ ->
                    implementation(
                        data,
                        userData ?: error("Cannot seed test data - TestUserData is not provided.")
                    )
                },
                // CreateUserQuarkResponse will have all user details after seeding
                // and is good for tear down.
                tearDown = { data, createUserQuarkResponse, _, _ ->
                    tearDown?.invoke(
                        this,
                        data,
                        createUserQuarkResponse
                            ?: error("Cannot tear down test data - CreateUserQuarkResponse is not provided.")
                    )
                }
            )
        }

        // Factory method for Map<String, TestUserData>.
        public fun <T : Annotation> forUserMap(
            default: T,
            implementation: QuarkCommand.(T, Map<String, TestUserData>) -> Unit,
            tearDown: (QuarkCommand.(T, Map<String, TestUserData>) -> Unit)? = null
        ): AnnotationTestData<T> {
            return AnnotationTestData(
                instance = default,
                implementation = { data, _, _, usersMap ->
                    implementation(
                        data,
                        usersMap ?: error("Cannot seed test data - TestUserData is not provided.")
                    )
                },
                tearDown = { data, _, _, usersMap ->
                    tearDown?.invoke(
                        this,
                        data,
                        usersMap ?: error("Cannot seed test data - TestUserData is not provided.")
                    )
                }
            )
        }
    }
}
