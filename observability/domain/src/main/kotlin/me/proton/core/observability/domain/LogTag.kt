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

package me.proton.core.observability.domain

public object LogTag {
    /** Default tag. */
    public const val DEFAULT: String = "core.observability"

    /** An observability event has been enqueued. */
    public const val ENQUEUE: String = "core.observability.enqueue"

    /** An unknown error has been mapped. */
    public const val UNKNOWN: String = "core.observability.error.unknown"

    /** A parse error has been mapped. */
    public const val PARSE: String = "core.observability.error.parse"
}
