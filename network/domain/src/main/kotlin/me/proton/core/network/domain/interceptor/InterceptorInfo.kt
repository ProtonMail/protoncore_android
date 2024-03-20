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

package me.proton.core.network.domain.interceptor

data class InterceptorInfo(
    val type: Type = Type.APP,
    val priority: Priority = Priority.MEDIUM,
) {
    enum class Type {
        APP, NETWORK,
    }

    @JvmInline
    value class Priority private constructor(val value: Int) : Comparable<Priority> {

        override fun compareTo(other: Priority): Int = value.compareTo(other.value)

         companion object {
             operator fun invoke(value: Int) = Priority(value.coerceIn(TOP.value, LEAST.value))

             val TOP = Priority(0)
             val MEDIUM = Priority(100_000_000)
             val LEAST = Priority(Int.MAX_VALUE)
         }
    }
}
