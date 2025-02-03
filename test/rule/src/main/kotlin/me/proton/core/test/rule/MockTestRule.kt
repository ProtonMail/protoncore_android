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

import me.proton.core.test.mockproxy.MockClient
import me.proton.core.test.rule.annotation.MockTest
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.util.kotlin.takeIfNotEmpty
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Responsible for mock-proxy behavior configuration.
 * Can act in test replay and record mode based on [shouldRecord] parameter.
 * Enables SRP mocking in replay mode.
 */
public class MockTestRule(
    private val mockClient: MockClient,
    private val shouldRecord: Boolean = false
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        val mockTest = description.getAnnotation(MockTest::class.java)
            ?: description.testClass.getAnnotation(MockTest::class.java)

        mockTest?.let {
            mockTest.scenario.let { scenario ->
                if (shouldRecord && mockTest.isReference) {
                    mockClient.setRecording(
                        true,
                        mockClient.getScenarioDirPath(scenario)
                    )
                } else {
                    description.getAnnotation(PrepareUser::class.java)?.let { user ->
                        mockClient.setSRPMock(
                            true,
                            // Fall back to the default password value as we have to
                            // pass password to SRP anyway here.
                            user.userData.password.takeIfNotEmpty() ?: "password"
                        )
                        mockClient.setScenarioFromAssets(scenario)
                    }
                }
            }
        }
        return base
    }
}
