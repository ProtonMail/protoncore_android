/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.passvalidator.data.repository

import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.passvalidator.data.api.PasswordPolicyApi
import me.proton.core.passvalidator.data.api.response.toPasswordPolicies
import me.proton.core.passvalidator.data.entity.PasswordPolicy
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject

@ActivityRetainedScoped
internal class PasswordPolicyRepository @Inject constructor(
    private val provider: ApiProvider
) {
    private val passwordPoliciesCache = Cache.Builder<String, List<PasswordPolicy>>().build()

    fun observePasswordPolicies(userId: UserId?, refresh: Boolean = false): Flow<List<PasswordPolicy>> = flow {
        emit(getExistingOrEmpty(userId))
        if (refresh) passwordPoliciesCache.invalidate(userId.cacheKey())
        emit(getExistingOrFetch(userId))
    }.distinctUntilChanged()

    private fun getExistingOrEmpty(
        userId: UserId?
    ): List<PasswordPolicy> = passwordPoliciesCache.get(userId.cacheKey()) ?: emptyList()

    private suspend fun getExistingOrFetch(
        userId: UserId?
    ): List<PasswordPolicy> = passwordPoliciesCache.get(userId.cacheKey()) {
        result("getPasswordPolicies") {
            provider.get<PasswordPolicyApi>(userId).invoke {
                getPasswordPolicies().toPasswordPolicies()
            }.valueOrThrow
        }
    }

    private fun UserId?.cacheKey(): String = this?.id ?: ""
}
