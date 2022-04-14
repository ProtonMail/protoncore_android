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

package me.proton.core.eventmanager.data.db

import androidx.room.TypeConverter
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.RefreshType
import me.proton.core.eventmanager.domain.entity.State
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize

class EventManagerConverters {
    @TypeConverter
    fun fromEventManagerConfigToString(value: EventManagerConfig?) = value?.serialize()

    @TypeConverter
    fun fromStringToEventManagerConfig(value: String?): EventManagerConfig? = value?.deserialize()

    @TypeConverter
    fun fromStateToString(value: State?) = value?.name

    @TypeConverter
    fun fromStringToState(value: String?): State? = State.enumOf(value)

    @TypeConverter
    fun fromRefreshTypeToString(value: RefreshType?) = value?.name

    @TypeConverter
    fun fromStringToRefreshType(value: String?): RefreshType? = RefreshType.enumOf(value)
}
