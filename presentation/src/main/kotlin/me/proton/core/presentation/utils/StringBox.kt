/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.presentation.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes

/** Represents a [String] either directly, or indirectly via Android Resource ID.
 * The class can be easily created without the need to pass a [android.content.Context],
 * but it needs to be provided when consuming the string.
 */
sealed interface StringBox {
    fun get(context: Context): String = get(context.resources)
    fun get(resources: Resources): String

    data class ResourceString(@StringRes val id: Int) : StringBox {
        override fun get(resources: Resources): String = resources.getString(id)
    }

    @JvmInline
    value class PlainString(val value: String) : StringBox {
        override fun get(resources: Resources): String = value
    }

    companion object {
        operator fun invoke(@StringRes id: Int) = ResourceString(id)
        operator fun invoke(value: String) = PlainString(value)
    }
}
