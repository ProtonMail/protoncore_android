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

package me.proton.core.label.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.NewLabel

interface LabelRepository {
    /**
     * Observe all [Label] for [userId], by [type].
     */
    fun observeLabels(userId: UserId, type: LabelType, refresh: Boolean = false): Flow<DataResult<List<Label>>>

    /**
     * Get all [Label] for [userId], by [type].
     */
    suspend fun getLabels(userId: UserId, type: LabelType, refresh: Boolean = false): List<Label>

    /**
     * Get a [Label] for [userId], by [type] and [labelId].
     */
    suspend fun getLabel(userId: UserId, type: LabelType, labelId: LabelId, refresh: Boolean = false): Label?

    /**
     * Create a new [Label] for [userId], remotely, then locally if success.
     */
    suspend fun createLabel(userId: UserId, label: NewLabel)

    /**
     * Update [Label] for [userId], locally, then remotely in background.
     */
    suspend fun updateLabel(userId: UserId, label: Label)

    /**
     * Update [Label.isExpanded] for [userId], locally, then remotely in background.
     */
    suspend fun updateLabelIsExpanded(userId: UserId, type: LabelType, labelId: LabelId, isExpanded: Boolean)

    /**
     * Delete label for [userId] by [labelId], locally, then remotely in background.
     */
    suspend fun deleteLabel(userId: UserId, type: LabelType, labelId: LabelId)

    /**
     * Mark local data as stale for [userId], by [type].
     *
     * Note: Repository will refresh data asap.
     */
    fun markAsStale(userId: UserId, type: LabelType)
}
