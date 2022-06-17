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

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.R
import me.proton.android.core.coreexample.adapter.PushAdapter
import me.proton.android.core.coreexample.databinding.ActivityPushesBinding
import me.proton.android.core.coreexample.viewmodel.PushesViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId

@AndroidEntryPoint
class PushesActivity : ProtonViewBindingActivity<ActivityPushesBinding>(ActivityPushesBinding::inflate) {

    private val viewModel: PushesViewModel by viewModels()
    private val pushAdapter = PushAdapter(::onClickPush)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recyclerView.adapter = pushAdapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.toolbar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        PushesViewModel.State.Loading -> onLoading()
                        is PushesViewModel.State.Pushes -> displayPushes(state.pushes)
                    }
                }
            }
        }
    }

    private fun onClickPush(userId: UserId, pushId: PushId) {
        startActivity(PushDetailsActivity.createIntent(this, userId, pushId))
    }

    private fun onLoading() {
        binding.toolbar.title = "Loading..."
    }

    private fun displayPushes(pushes: List<Push>) {
        pushAdapter.submitList(pushes)
        binding.toolbar.title = resources.getQuantityString(R.plurals.pushes_activity_title, pushes.size, pushes.size)
    }
}
