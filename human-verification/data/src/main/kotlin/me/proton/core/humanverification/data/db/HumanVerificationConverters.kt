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

package me.proton.core.humanverification.data.db

import androidx.room.TypeConverter
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.client.ClientIdType

class HumanVerificationConverters {

    @TypeConverter
    fun fromHumanVerificationStateToString(value: HumanVerificationState?): String? = value?.name

    @TypeConverter
    fun fromStringToHumanVerificationState(value: String?): HumanVerificationState? = value?.let {
        HumanVerificationState.valueOf(value)
    }

    @TypeConverter
    fun fromClientIdTypeToString(value: ClientIdType?): String? = value?.value?.lowercase()

    @TypeConverter
    fun fromStringToClientIdType(value: String?): ClientIdType? = value?.let {
        ClientIdType.map[value.lowercase()]
    }

}
