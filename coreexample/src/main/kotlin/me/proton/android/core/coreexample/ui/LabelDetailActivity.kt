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

package me.proton.android.core.coreexample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.core.coreexample.databinding.ActivityLabelDetailsBinding
import me.proton.android.core.coreexample.viewmodel.LabelDetailViewModel
import me.proton.android.core.coreexample.viewmodel.LabelDetailViewModel.Companion.ARG_LABEL_ID
import me.proton.android.core.coreexample.viewmodel.LabelDetailViewModel.Companion.ARG_LABEL_TYPE
import me.proton.android.core.coreexample.viewmodel.LabelDetailViewModel.Companion.ARG_USER_ID
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class LabelDetailActivity :
    ProtonViewBindingActivity<ActivityLabelDetailsBinding>(ActivityLabelDetailsBinding::inflate) {

    private val viewModel: LabelDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.updateButton.setOnClickListener { viewModel.dispatch(LabelDetailViewModel.Action.Update) }
        binding.deleteButton.setOnClickListener { viewModel.dispatch(LabelDetailViewModel.Action.Delete) }

        viewModel.state.flowWithLifecycle(lifecycle).onEach { state ->
            when (state) {
                is LabelDetailViewModel.State.Loading -> {
                    setLoading(true)
                }
                is LabelDetailViewModel.State.Success -> {
                    setLoading(false)
                    setLabel(state.rawLabel)
                }
                is LabelDetailViewModel.State.Error -> {
                    setLoading(false)
                    showToast(state.error ?: "An error occurred")
                }
                is LabelDetailViewModel.State.Deleted -> {
                    setLoading(false)
                    showToast("Deleted!")
                    finish()
                }
                LabelDetailViewModel.State.Updated -> {
                    setLoading(false)
                    showToast("Updated!")
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun setLabel(raw: String) {
        binding.rawLabel.text = raw
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progress.isVisible = isLoading
        binding.deleteButton.isClickable = !isLoading
    }

    companion object {
        fun createIntent(context: Context, userId: UserId, labelId: LabelId, type: LabelType) =
            Intent(context, LabelDetailActivity::class.java).apply {
                putExtra(ARG_USER_ID, userId.id)
                putExtra(ARG_LABEL_ID, labelId.id)
                putExtra(ARG_LABEL_TYPE, type.value)
            }
    }
}
