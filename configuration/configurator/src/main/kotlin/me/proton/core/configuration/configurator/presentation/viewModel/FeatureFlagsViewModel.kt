/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.configuration.configurator.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.configuration.configurator.featureflag.data.FeatureFlagsCacheManager
import me.proton.core.configuration.configurator.featureflag.data.api.Feature

class FeatureFlagsViewModel : ViewModel() {
    private val _featureFlags = MutableStateFlow<List<Feature>>(emptyList())
    val featureFlags: StateFlow<List<Feature>> = _featureFlags.asStateFlow()
    private var currentProject: String? = null

    fun loadFeatureFlagsByProject(project: String) = viewModelScope.launch {
        currentProject = project
        val flags = FeatureFlagsCacheManager().getFeatureFlags()
        _featureFlags.value = flags.filter { it.project == project }
    }
}
