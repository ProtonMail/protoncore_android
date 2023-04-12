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

package me.proton.core.keytransparency.domain.exception

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
public fun keyTransparencyCheck(value: Boolean) {
    contract {
        returns() implies value
    }
    keyTransparencyCheck(value) { "Check failed." }
}

/**
 * Throws an [KeyTransparencyException] with the result of calling [lazyMessage] if the [value] is false.
 *
 */
@OptIn(ExperimentalContracts::class)
public fun keyTransparencyCheck(value: Boolean, lazyMessage: () -> Any) {
    contract {
        returns() implies value
    }
    if (!value) {
        val message = lazyMessage()
        throw KeyTransparencyException(message.toString())
    }
}

/**
 * Throws an [KeyTransparencyException] if the [value] is null. Otherwise
 * returns the not null value.
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> keyTransparencyCheckNotNull(value: T?): T {
    contract {
        returns() implies (value != null)
    }
    return keyTransparencyCheckNotNull(value) { "Required value was null." }
}

/**
 * Throws an [KeyTransparencyException] with the result of calling [lazyMessage]  if the [value] is null. Otherwise
 * returns the not null value.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <T : Any> keyTransparencyCheckNotNull(value: T?, lazyMessage: () -> Any): T {
    contract {
        returns() implies (value != null)
    }

    if (value == null) {
        val message = lazyMessage()
        throw KeyTransparencyException(message.toString())
    } else {
        return value
    }
}
