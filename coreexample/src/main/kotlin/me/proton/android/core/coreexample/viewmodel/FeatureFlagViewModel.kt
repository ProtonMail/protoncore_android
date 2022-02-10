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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.utils.ClientFeatureFlags
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.presentation.viewmodel.ViewModelResult
import javax.inject.Inject

@HiltViewModel
class FeatureFlagViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val featureFlagRepository: FeatureFlagRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow<ViewModelResult<FeatureFlag>>(ViewModelResult.Processing)
    val state = mutableState.asStateFlow()

    fun isFeatureEnabled(featureId: FeatureId) = viewModelScope.launch {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        featureFlagRepository.observe(userId, featureId).mapLatest { featureFlag ->
            val result = featureFlag ?: defaultValueOf(featureId)
            mutableState.emit(ViewModelResult.Success(result))

        }.launchIn(viewModelScope)
    }

    private fun defaultValueOf(featureId: FeatureId) =
        FeatureFlag(
            featureId,
            ClientFeatureFlags.values().first { it.id == featureId }.defaultLocalValue
        )
}
