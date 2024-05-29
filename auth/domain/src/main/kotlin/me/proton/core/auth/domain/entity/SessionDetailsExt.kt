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

package me.proton.core.auth.domain.entity

import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationOptions
import me.proton.core.util.kotlin.deserializeOrNull

val SessionDetails.fido2AuthenticationOptions: Fido2AuthenticationOptions?
    get() = fido2AuthenticationOptionsJson?.deserializeOrNull()
