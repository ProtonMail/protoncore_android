package me.proton.core.test.quark.v2.command

import me.proton.core.test.quark.v2.QuarkCommand
import okhttp3.Response

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

public const val JAIL_UNBAN: String = "quark/raw::jail:unban"

public fun QuarkCommand.jailUnban(): Response = route(JAIL_UNBAN).build().let { client.executeQuarkRequest(it) }
