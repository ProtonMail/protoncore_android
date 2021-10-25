/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.eventmanager.data.extension

import me.proton.core.eventmanager.domain.EventListener

suspend fun <R> Collection<EventListener<*, *>>.inTransaction(block: suspend () -> R): R {
    return when {
        isEmpty() -> block()
        // Base condition.
        count() == 1 -> last().inTransaction(block)
        else -> first().inTransaction {
            // Recursion.
            drop(1).inTransaction(block)
        }
    }
}
