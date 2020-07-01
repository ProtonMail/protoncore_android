/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.android.core.domain.data

import me.proton.android.core.domain.entity.Keys
import me.proton.android.core.domain.entity.User

/**
 * Created by dinokadrikj on 4/23/20.
 *
 * The clients can extend this class to provide what is specific for their application regarding [User]
 * data.
 * This is a base data that [User] must hold in order to make the common stuff working properly.
 */
interface UserSettings {
    // region abstract fields
    val userName: String?
    val keys: List<Keys?>?
    // endregion

    fun load(username: String): User?
}