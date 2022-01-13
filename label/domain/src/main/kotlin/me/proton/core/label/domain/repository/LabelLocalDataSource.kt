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

package me.proton.core.label.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType

interface LabelLocalDataSource {

    /**
     * Observe all [Label] for [userId], by [type].
     */
    fun observeLabels(userId: UserId, type: LabelType): Flow<List<Label>>

    /**
     * Get all [Label] for [userId], by [type].
     */
    suspend fun getLabels(userId: UserId, type: LabelType): List<Label>

    /**
     * Update or insert [Label].
     */
    suspend fun upsertLabel(labels: List<Label>)

    /**
     * Delete Label(s) for [userId], by [labelIds].
     */
    suspend fun deleteLabel(userId: UserId, labelIds: List<LabelId>)

    /**
     * Delete all label for [userId].
     */
    suspend fun deleteAllLabels(userId: UserId)
}
