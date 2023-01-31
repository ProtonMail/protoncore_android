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

package me.proton.core.observability.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.proton.core.observability.domain.entity.ObservabilityEvent
import java.time.Instant

@Entity
public data class ObservabilityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val version: Long,
    val timestamp: Long,
    val data: String // json
) {
    internal fun toObservabilityEvent() = ObservabilityEvent(
        id = this.id,
        name = this.name,
        version = this.version,
        data = this.data,
        timestamp = Instant.ofEpochSecond(this.timestamp)
    )
}