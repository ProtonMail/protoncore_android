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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.R
import me.proton.android.core.coreexample.databinding.ActivityCreateContactBinding
import me.proton.android.core.coreexample.viewmodel.CreateContactViewModel
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.util.kotlin.CoreLogger

@AndroidEntryPoint
class CreateContactActivity : ProtonActivity<ActivityCreateContactBinding>() {
    override fun layoutId(): Int = R.layout.activity_create_contact

    private val viewModel: CreateContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.createButton.setOnClickListener {
            viewModel.createContact(binding.name.text.toString())
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    CoreLogger.d("contact", it.toString())
                }
            }
        }
    }
}
