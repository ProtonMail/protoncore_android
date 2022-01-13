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

package me.proton.core.label.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.repository.LabelLocalDataSource
import javax.inject.Inject

class LabelLocalDataSourceImpl @Inject constructor(
    labelDatabase: LabelDatabase
) : LabelLocalDataSource {

    private val labelDao = labelDatabase.labelDao()

    override fun observeLabels(userId: UserId, type: LabelType): Flow<List<Label>> =
        labelDao.observeAll(userId, type.value).map { list -> list.map { it.toLabel() } }

    override suspend fun getLabels(userId: UserId, type: LabelType): List<Label> =
        labelDao.getAll(userId, type.value).map { it.toLabel() }

    override suspend fun upsertLabel(labels: List<Label>) =
        labelDao.insertOrUpdate(*labels.map { it.toEntity() }.toTypedArray())

    override suspend fun deleteLabel(userId: UserId, labelIds: List<LabelId>) =
        labelDao.delete(userId, labelIds.map { it.id })

    override suspend fun deleteAllLabels(userId: UserId) =
        labelDao.deleteAll(userId)
}
