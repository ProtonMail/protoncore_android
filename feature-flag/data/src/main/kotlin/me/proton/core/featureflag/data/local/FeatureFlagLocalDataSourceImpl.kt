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

package me.proton.core.featureflag.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import javax.inject.Inject

public class FeatureFlagLocalDataSourceImpl @Inject constructor(
    private val db: FeatureFlagDatabase
) : FeatureFlagLocalDataSource {

    private val dao = db.featureFlagDao()

    override suspend fun getAll(scope: Scope): List<FeatureFlag> =
        dao.getAll(scope).map { it.toFeatureFlag() }

    override suspend fun replaceAll(userId: UserId?, scope: Scope, flags: List<FeatureFlag>) {
        check(flags.all { it.scope == scope && it.userId == userId })
        db.inTransaction {
            dao.deleteAll(userId.orGlobal(), scope)
            dao.insertOrUpdate(*flags.map { it.toEntity() }.toTypedArray())
        }
    }

    override fun observe(userId: UserId?, scope: Scope): Flow<List<FeatureFlag>> =
        dao.observe(userId.withGlobal(), scope)
            .map { list -> list.map { it.toFeatureFlag() } }

    override fun observe(userId: UserId?, featureIds: Set<FeatureId>): Flow<List<FeatureFlag>> =
        dao.observe(userId.withGlobal(), featureIds.map { it.id })
            .map { list -> list.sortedBy { flag -> flag.scope.value }.map { it.toFeatureFlag() } }

    override suspend fun upsert(flags: List<FeatureFlag>): Unit =
        dao.insertOrUpdate(*flags.map { it.toEntity() }.toTypedArray())

    override suspend fun updateValue(userId: UserId?, featureId: FeatureId, value: Boolean) {
        dao.updateValue(userId.orGlobal(), featureId.id, value)
    }
}
