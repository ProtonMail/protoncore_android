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

package me.proton.core.auth.fido.domain.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.proton.core.auth.fido.domain.entity.Fido2AuthenticationExtensionsClientInputs

public fun Fido2AuthenticationExtensionsClientInputs.toJson(): JsonObject? = JsonObject(
    buildMap {
        if (appId != null) {
            put("appid", JsonPrimitive(appId))
        }
        if (thirdPartyPayment != null) {
            put("thirdPartyPayment", JsonPrimitive(thirdPartyPayment))
        }
        if (uvm != null) {
            put("uvm", JsonPrimitive(uvm))
        }
    }
).takeIf { it.isNotEmpty() }
