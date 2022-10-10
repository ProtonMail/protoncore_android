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

package me.proton.core.label.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.label.data.local.LabelDatabase
import me.proton.core.label.data.remote.resource.LabelResource
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.repository.LabelLocalDataSource
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.util.kotlin.deserialize
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LabelsEvents(
    @SerialName("Labels")
    val labels: List<LabelEvent>? = null
)

@Serializable
data class LabelEvent(
    @SerialName("ID")
    val id: String,
    @SerialName("Action")
    val action: Int,
    @SerialName("Label")
    val label: LabelResource? = null
)

@Singleton
open class LabelEventListener @Inject constructor(
    private val db: LabelDatabase,
    private val labelLocalDataSource: LabelLocalDataSource,
    private val labelRepository: LabelRepository
) : EventListener<String, LabelResource>() {

    override val type = Type.Core
    override val order = 1

    override suspend fun deserializeEvents(
        config: EventManagerConfig,
        response: EventsResponse
    ): List<Event<String, LabelResource>>? {
        return response.body.deserialize<LabelsEvents>().labels?.map {
            Event(requireNotNull(Action.map[it.action]), it.id, it.label)
        }
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = db.inTransaction(block)

    override suspend fun onCreate(config: EventManagerConfig, entities: List<LabelResource>) {
        labelLocalDataSource.upsertLabel(entities.map { it.toLabel(config.userId) })
    }

    override suspend fun onUpdate(config: EventManagerConfig, entities: List<LabelResource>) {
        labelLocalDataSource.upsertLabel(entities.map { it.toLabel(config.userId) })
    }

    override suspend fun onDelete(config: EventManagerConfig, keys: List<String>) {
        labelLocalDataSource.deleteLabel(config.userId, keys.map { LabelId(it) })
    }

    override suspend fun onResetAll(config: EventManagerConfig) {
        labelLocalDataSource.deleteAllLabels(config.userId)
        LabelType.values().forEach { type ->
            labelRepository.getLabels(config.userId, type, refresh = true)
        }
    }
}
