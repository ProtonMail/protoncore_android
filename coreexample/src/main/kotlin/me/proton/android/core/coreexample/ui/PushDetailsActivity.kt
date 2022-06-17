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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.databinding.ActivityPushDetailsBinding
import me.proton.android.core.coreexample.utils.prettyPrint
import me.proton.android.core.coreexample.viewmodel.PushDetailsViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.showToast
import me.proton.core.push.domain.entity.PushId
import me.proton.core.util.kotlin.CoreLogger

@AndroidEntryPoint
class PushDetailsActivity : ProtonViewBindingActivity<ActivityPushDetailsBinding>(ActivityPushDetailsBinding::inflate) {

    private val viewModel: PushDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is PushDetailsViewModel.State.PushContent -> binding.pushContent.text = state.push.prettyPrint()
                        is PushDetailsViewModel.State.Finish -> {
                            showToast(state.reason)
                            finish()
                        }
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener { viewModel.onClickDeletePush() }
        binding.markReadButton.setOnClickListener { viewModel.onClickMarkAsRead() }

        CoreLogger.v("debug", viewModel.toString())
    }

    companion object {
        fun createIntent(context: Context, userId: UserId, pushId: PushId) =
            Intent(context, PushDetailsActivity::class.java).apply {
                putExtra(PushDetailsViewModel.ARG_USER_ID, userId.id)
                putExtra(PushDetailsViewModel.ARG_PUSH_ID, pushId.id)
            }
    }
}
