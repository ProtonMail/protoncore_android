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

package me.proton.core.auth.domain

object LogTag {
    const val INVALID_SRP_PROOF = "core.auth.domain.srp.invalid.server.proof"
    const val PERFORM_SUBSCRIBE = "core.auth.domain.perform.subscribe"
    const val ORGANIZATION_LOAD = "core.auth.domain.organization.load"
    const val UNLOCK_USER = "core.auth.domain.perform.backuppass.unlockuser"
    const val ACTIVATE_DEVICE = "core.auth.domain.perform.backuppass.activate"
    const val UNPRIVATIZE_USER = "core.auth.domain.perform.backuppass.unprivatize"
    const val SETUP_KEYS = "core.auth.domain.perform.backuppass.setup.keys"
}
