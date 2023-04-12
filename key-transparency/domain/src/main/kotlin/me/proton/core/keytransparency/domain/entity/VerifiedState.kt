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

package me.proton.core.keytransparency.domain.entity

internal interface TimedState {
    val notBefore: Long
}

/**
 * The different states of inclusion of an address in KT
 */
public sealed class VerifiedState {
    /**
     * The address is included in KT
     */
    public data class Existent(override val notBefore: Long) : VerifiedState(), TimedState

    /**
     * The address is not in KT
     */
    public data class Absent(override val notBefore: Long) : VerifiedState(), TimedState

    /**
     * The address was in KT and then was removed (after the address was disabled)
     */
    public data class Obsolete(override val notBefore: Long) : VerifiedState(), TimedState

    /**
     * The address is not yet in KT
     */
    public object NotYetIncluded : VerifiedState()
}
