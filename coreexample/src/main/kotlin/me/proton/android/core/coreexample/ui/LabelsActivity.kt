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

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.adapter.LabelsAdapter
import me.proton.android.core.coreexample.databinding.ActivityLabelsBinding
import me.proton.android.core.coreexample.viewmodel.LabelsViewModel
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class LabelsActivity :
    ProtonViewBindingActivity<ActivityLabelsBinding>(ActivityLabelsBinding::inflate) {

    private val viewModel: LabelsViewModel by viewModels()
    private val labelsAdapter = LabelsAdapter(::onClickContact)

    @Inject
    lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.labelsRecyclerView.adapter = labelsAdapter
        binding.addButton.setOnClickListener { startActivity(Intent(this, CreateLabelActivity::class.java)) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is LabelsViewModel.State.Labels -> labelsAdapter.submitList(state.labels)
                        is LabelsViewModel.State.Error -> showToast(state.reason)
                        is LabelsViewModel.State.Processing -> showToast("processing")
                    }.exhaustive
                }
            }
        }
    }

    private fun onClickContact(labelId: LabelId, type: LabelType) {
        lifecycleScope.launch {
            val userId = requireNotNull(accountManager.getPrimaryUserId().first())
            startActivity(LabelDetailActivity.createIntent(this@LabelsActivity, userId, labelId, type))
        }
    }
}
