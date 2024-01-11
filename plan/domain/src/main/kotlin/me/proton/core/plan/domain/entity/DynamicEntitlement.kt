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

package me.proton.core.plan.domain.entity

sealed class DynamicEntitlement {
    data class Description(
        val text: String,
        val iconUrl: String,
        val hint: String? = null,
    ) : DynamicEntitlement()

    data class Progress(
        val text: String,
        val current: Long,
        val min: Long,
        val max: Long,
        val tag: String?
    ) : DynamicEntitlement() {
        companion object Tag {
            const val Base: String = "base"
            const val Drive: String = "drive"
        }
    }
}
