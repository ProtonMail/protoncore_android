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

package me.proton.configuration.provider.data

import me.proton.core.configuration.entity.ConfigContract

class MockConfigWithNullValues {
    val nullableString: String? = null
}

class EmptyMockConfig

@Suppress("unused")
class MockConfigWithNonPublicProps {
    private val privateString: String = "private"
}

class MockConfigWithOtherTypes {
    val integerProp: Int = 123
}

class MockConfigWithUnexpectedValueType {
    val host: Int = 123
}

class MockEnvironmentConfig : ConfigContract {
    override val host: String = "host"
    override val proxyToken: String = "proxyToken"
    override val apiPrefix: String = "apiPrefix"
    override val baseUrl: String? = null
    override val apiHost: String = "apiHost"
    override val hv3Host: String = "hv3Host"
    override val hv3Url: String = "hv3Url"
    override val useDefaultPins: Boolean = false
}
