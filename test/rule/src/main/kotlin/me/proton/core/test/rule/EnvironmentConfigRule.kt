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
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.configContractFieldsMap
import me.proton.core.test.rule.di.TestEnvironmentConfigModule.overrideConfig
import me.proton.core.test.rule.di.TestEnvironmentConfigModule.provideEnvironmentConfiguration
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A test rule for setting up environment configuration before running tests.
 *
 * @property defaultConfig The default [EnvironmentConfig] to use for tests if no overrides are specified.
 * By default, it uses a configuration provided by [provideEnvironmentConfiguration].
 */
public class EnvironmentConfigRule(
    private val defaultConfig: EnvironmentConfig =
        EnvironmentConfig.fromConfiguration(provideEnvironmentConfiguration())
) : TestRule {

    /**
     * The active [EnvironmentConfig] for the current test. It is determined by looking for an
     * [EnvironmentConfig] annotation on the test method or class. If not found, [defaultConfig] is used.
     *
     * This property is initialized when the rule is applied and is accessible during the test execution.
     */
    public lateinit var config: EnvironmentConfig
        private set

    /**
     * Applies the environment configuration for the test described by [description].
     */
    override fun apply(base: Statement, description: Description): Statement {
        config = description.getAnnotation(EnvironmentConfig::class.java) ?: defaultConfig
        EnvironmentConfiguration.fromMap(config.configContractFieldsMap).apply(overrideConfig::set)
        return base
    }
}
