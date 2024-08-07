/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.label.data.local

import androidx.room.TypeConverter
import me.proton.core.label.domain.entity.LabelId

class LabelConverters {

    @TypeConverter
    fun fromLabelIdToString(value: LabelId?): String? = value?.id

    @TypeConverter
    fun fromStringToLabelId(value: String?): LabelId? = value?.let { LabelId(value) }
}
