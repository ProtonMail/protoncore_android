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

package me.proton.core.keytransparency.domain

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

public object Constants {
    internal val KT_MAX_EPOCH_INTERVAL_SECONDS = 72.hours.inWholeSeconds
    /**
     * Specifies the period at which self audit should run
     * in hours
     */
    public const val KT_SELF_AUDIT_INTERVAL_HOURS: Int = 4
    public val KT_SELF_AUDIT_INTERVAL_SECONDS: Long = KT_SELF_AUDIT_INTERVAL_HOURS.hours.inWholeSeconds
    internal val KT_EPOCH_VALIDITY_PERIOD_SECONDS = 90.days.inWholeSeconds
    internal const val KT_VERIFIED_EPOCH_SIGNATURE_CONTEXT: String = "key-transparency.verified-epoch"
}
