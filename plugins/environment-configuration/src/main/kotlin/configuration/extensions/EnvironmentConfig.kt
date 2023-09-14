/*
 * Copyright (c) 2022 Proton Technologies AG
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

package configuration.extensions

import configuration.EnvironmentConfig
import configuration.ProtonEnvironmentConfigurationPlugin.Companion.DEFAULTS_CLASS_NAME
import configuration.ProtonEnvironmentConfigurationPlugin.Companion.PACKAGE_NAME
import configuration.util.toBuildConfigValue

fun EnvironmentConfig.mergeWith(other: EnvironmentConfig) = EnvironmentConfig(
    host = other.host ?: host,
    apiPrefix = other.apiPrefix ?: apiPrefix,
    baseUrl = other.baseUrl ?: baseUrl,
    apiHost = other.apiHost ?: apiHost,
    hv3Host = other.hv3Host ?: hv3Host,
    hv3Url = other.hv3Url ?: hv3Url,
    proxyToken = other.proxyToken.takeIf { it?.isNotEmpty()!! } ?: proxyToken,
)

fun EnvironmentConfig.sourceClassContent(
    namespace: String = PACKAGE_NAME,
    className: String = DEFAULTS_CLASS_NAME,
    fields: String = configData.joinToString("\n")
): String = """package $namespace;

public class $className {
$fields
}
"""

private val EnvironmentConfig.configData: List<String>
    get() = EnvironmentConfig::class.java.declaredFields.map {
        it.trySetAccessible()

        val (type, name) = it.type.simpleName to it.name
        val value = it.get(this).toBuildConfigValue()

        "    public static final $type $name = $value;"
    }
