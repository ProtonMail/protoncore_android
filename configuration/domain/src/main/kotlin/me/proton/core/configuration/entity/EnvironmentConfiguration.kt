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

package me.proton.core.configuration.entity

import kotlin.reflect.KProperty

public data class EnvironmentConfiguration(
    val configFieldProvider: ConfigFieldProvider
) : ConfigContract {
    override val host: String? by provide(this::host)
    override val proxyToken: String? by provide(this::proxyToken)
    override val apiPrefix: String? by provide(this::apiPrefix)
    override val baseUrl: String? by provide(this::baseUrl)
    override val apiHost: String? by provide(this::apiHost)
    override val hv3Host: String? by provide(this::hv3Host)
    override val hv3Url: String? by provide(this::hv3Url)
    override val useDefaultPins: Boolean? by provide(this::useDefaultPins)

    public inline fun <reified T> provide(property: KProperty<*>): Lazy<T?> = lazy {
        when (T::class) {
            String::class -> configFieldProvider.stringProvider(property.name) as? T
            Boolean::class -> configFieldProvider.booleanProvider(property.name) as? T
            else -> throw IllegalArgumentException("Unsupported Environment Configuration type")
        }
    }
}
