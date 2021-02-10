/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityCreateAddressBinding
import me.proton.core.auth.presentation.entity.CreateAddressInput
import me.proton.core.auth.presentation.viewmodel.CreateAddressViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.utils.onClick

/**
 * Second step in the address creation flow.
 *
 * Displays the results from the username availability and triggers the business logic along with all API executions.
 */
@AndroidEntryPoint
class CreateAddressActivity : AuthActivity<ActivityCreateAddressBinding>() {

    private val input: CreateAddressInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val viewModel by viewModels<CreateAddressViewModel>()

    override fun layoutId(): Int = R.layout.activity_create_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                finish()
            }
            createAddressButton.onClick {
                viewModel.upgradeAccount(UserId(input.userId), input.username, input.domain)
            }
            externalEmailText.visibility = View.GONE
            titleText.text = String.format(
                getString(
                    R.string.auth_create_address_result_title_username,
                    input.username,
                    input.domain
                )
            )
            //titleText.text = String.format(getString(R.string.auth_create_address_result_title), username)
            termsConditionsText.movementMethod = LinkMovementMethod.getInstance()
        }

        viewModel.upgradeState.observeData {
            when (it) {
                is CreateAddressViewModel.State.Processing -> showLoading(true)
                is CreateAddressViewModel.State.Success -> onSuccess()
                is CreateAddressViewModel.State.Error.Message -> showError(it.message)
            }
        }
    }

    override fun showLoading(loading: Boolean) = with(binding.createAddressButton) {
        title = if (loading) {
            setLoading()
            getString(R.string.auth_create_address_creating)
        } else {
            setIdle()
            getString(R.string.auth_create_address_create)
        }
    }

    private fun onSuccess() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
