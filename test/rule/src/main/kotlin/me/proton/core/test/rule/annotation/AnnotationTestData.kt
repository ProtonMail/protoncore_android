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
public class AnnotationTestData<out T: Annotation>(
    public val instance: T,
    public val implementation: QuarkCommand.(@UnsafeVariance T, TestUserData?, CreateUserQuarkResponse?) -> Any,
    public val tearDown: (QuarkCommand.(@UnsafeVariance T, TestUserData?, CreateUserQuarkResponse?) -> Any?)? = null,
) {

    /**
     * Convenience constructor allowing specifying implementation without requiring test user data.
     *
     * @param default The default annotation instance to use.
     * @param implementation A lambda function defining how to apply the test data using the provided Quark command
     *                       and test data annotation.
     * @param tearDown An optional lambda function defining how to tear down the test data after the test,
     *                  using the provided Quark command and test data annotation.
     */
    public constructor(
        default: T,
        implementation: QuarkCommand.(T) -> Any,
        tearDown: (QuarkCommand.(T) -> Any)? = null
    ) : this(
        instance = default,
        implementation = { data, _, _ -> implementation(data) },
        tearDown = { data, _, _ -> tearDown?.invoke(this, data) }
    )

    /**
     * Convenience constructor allowing specifying implementation with only CreateUserQuarkResponse.
     *
     * @param default The default annotation instance to use.
     * @param implementation A lambda function defining how to apply the test data using the provided Quark command
     *                       and created user response.
     * @param tearDown An optional lambda function defining how to tear down the test data after the test,
     *                  using the provided Quark command and created user response.
     */
    public constructor(
        default: T,
        implementation: QuarkCommand.(T, CreateUserQuarkResponse) -> Any,
        tearDown: (QuarkCommand.(T, CreateUserQuarkResponse) -> Unit)? = null
    ) : this(
        instance = default,
        implementation = { data, _, seededUser ->
            implementation(
                data,
                seededUser ?: error("Cannot seed test data - TestUserData is not provided.")
            )
        },
        tearDown = { data, _, seededUser ->
            tearDown?.invoke(
                this,
                data,
                seededUser ?: error("Cannot tear down test data - TestUserData is not provided.")
            )
        }
    )
}
