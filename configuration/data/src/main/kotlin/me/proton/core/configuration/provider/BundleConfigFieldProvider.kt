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

package me.proton.core.configuration.provider

import android.os.Bundle
import me.proton.core.configuration.entity.EnvironmentConfigFieldProvider

public class BundleConfigFieldProvider(
    private val bundle: Bundle
) : EnvironmentConfigFieldProvider {
    override fun getString(key: String): String? = bundle.getString(key)

    override fun getBoolean(key: String): Boolean? =
        bundle.takeIf { bundle.containsKey(key) }?.getBoolean(key)

    override fun getInt(key: String): Int? = bundle.takeIf { bundle.containsKey(key) }?.getInt(key)
}
