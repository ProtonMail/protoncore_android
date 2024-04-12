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

import me.proton.core.configuration.extension.primitiveFieldMap
import me.proton.core.test.rule.annotation.EnvironmentConfig
import me.proton.core.test.rule.annotation.toEnvironmentConfiguration
import me.proton.core.test.rule.di.TestEnvironmentConfigModule.overrideEnvironmentConfiguration
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A test rule for setting up environment configuration before running tests.
 *
 * @property ruleConfig The default [EnvironmentConfig] to use for tests if no overrides are specified.
 */
public class EnvironmentConfigRule(
    private val ruleConfig: EnvironmentConfig?
) : TestWatcher() {
    public override fun starting(description: Description) {
        val annotationConfig = description.getAnnotation(EnvironmentConfig::class.java)
        val overrideConfig = annotationConfig ?: ruleConfig ?: return
        val overrideEnvironmentConfig = overrideConfig.toEnvironmentConfiguration()

        overrideEnvironmentConfiguration.set(overrideEnvironmentConfig)

        val overrideString = if (annotationConfig != null) "@EnvironmentConfig annotation" else "ProtonRule argument"

        printInfo("Overriding EnvironmentConfiguration with $overrideString: ${overrideEnvironmentConfig.primitiveFieldMap}")
    }
}
