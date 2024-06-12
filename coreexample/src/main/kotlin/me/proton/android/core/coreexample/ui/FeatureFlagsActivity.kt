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

package me.proton.android.core.coreexample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.databinding.ActivityFeatureFlagsBinding
import me.proton.android.core.coreexample.viewmodel.FeatureFlagsViewModel
import me.proton.core.presentation.ui.ProtonViewBindingActivity

@AndroidEntryPoint
class FeatureFlagsActivity :
    ProtonViewBindingActivity<ActivityFeatureFlagsBinding>(ActivityFeatureFlagsBinding::inflate) {
    private val viewModel: FeatureFlagsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.state
            .onEach(this::handleState)
            .launchIn(lifecycleScope)
        lifecycleScope.launch {
            viewModel.perform(FeatureFlagsViewModel.Action.Load)
        }
    }

    private fun handleState(state: FeatureFlagsViewModel.State) {
        binding.flagsError.isVisible = state.isError
        binding.progress.isVisible = state.isLoading
        binding.flagsEmpty.isVisible = !state.isLoading && state.featureFlags.isEmpty()
        binding.featureFlagsList.isVisible = state.featureFlags.isNotEmpty()

        if (state.featureFlags.isNotEmpty()) {
            val items = state.featureFlags.map {
                "${it.featureId.id} = ${it.value}"
            }
            binding.featureFlagsList.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                items
            )
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, FeatureFlagsActivity::class.java)
    }
}
