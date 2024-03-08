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

import me.proton.core.configuration.EnvironmentConfiguration


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class EnvironmentConfig(
    public val host: String
) {
    public companion object {
        public fun fromConfiguration(config: EnvironmentConfiguration): EnvironmentConfig =
            EnvironmentConfig(config.host)
    }
}

public val EnvironmentConfig.configContractFieldsMap: Map<String, String?>
    get() = mapOf(::host.name to host)

public fun EnvironmentConfig.toEnvironmentConfiguration(): EnvironmentConfiguration =
    EnvironmentConfiguration.fromMap(configContractFieldsMap)