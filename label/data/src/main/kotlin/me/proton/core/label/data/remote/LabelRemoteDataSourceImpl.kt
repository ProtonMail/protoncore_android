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

package me.proton.core.label.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.label.data.remote.request.toCreateLabelRequest
import me.proton.core.label.data.remote.request.toUpdateLabelRequest
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.NewLabel
import me.proton.core.label.domain.entity.UpdateLabel
import me.proton.core.label.domain.repository.LabelRemoteDataSource
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import javax.inject.Inject

class LabelRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
) : LabelRemoteDataSource {

    override suspend fun getLabels(userId: UserId, type: LabelType): List<Label> =
        apiProvider.get<LabelApi>(userId).invoke {
            getLabels(type.value).labels.map { it.toLabel(userId) }
        }.valueOrThrow

    override suspend fun createLabel(userId: UserId, label: NewLabel): Label =
        apiProvider.get<LabelApi>(userId).invoke {
            createLabel(label.toCreateLabelRequest()).label.toLabel(userId)
        }.valueOrThrow

    override suspend fun updateLabel(userId: UserId, label: UpdateLabel): Label =
        apiProvider.get<LabelApi>(userId).invoke {
            updateLabel(label.labelId.id, label.toUpdateLabelRequest()).label.toLabel(userId)
        }.valueOrThrow

    override suspend fun deleteLabel(userId: UserId, labelId: LabelId) =
        apiProvider.get<LabelApi>(userId).invoke {
            deleteLabel(labelId.id).isSuccess()
        }.throwIfError()
}
