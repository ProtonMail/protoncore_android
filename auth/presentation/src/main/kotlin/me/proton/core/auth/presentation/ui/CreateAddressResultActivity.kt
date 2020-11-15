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
import me.proton.core.presentation.utils.onClick
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UpdateExternalAccount
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityCreateAddressResultBinding
import me.proton.core.auth.presentation.entity.AddressesResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.viewmodel.CreateAddressResultViewModel
import me.proton.core.network.domain.session.SessionId

/**
 * Second step in the address creation flow. Displays the results from the username availability and triggers the
 * business logic along with all API executions.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class CreateAddressResultActivity : AuthActivity<ActivityCreateAddressResultBinding>() {

    private val sessionId: SessionId by lazy {
        SessionId(requireNotNull(intent?.extras?.getString(ARG_SESSION_ID)))
    }

    private val user: UserResult by lazy {
        intent?.extras?.get(ARG_USER) as UserResult
    }

    private val username: String by lazy {
        intent?.extras?.get(ARG_USERNAME) as String
    }

    private val externalEmail: String? by lazy {
        intent?.extras?.get(ARG_EXTERNAL_EMAIL) as String?
    }

    private val domain: String? by lazy {
        intent?.extras?.get(ARG_DOMAIN) as String?
    }

    private val viewModel by viewModels<CreateAddressResultViewModel>()

    override fun layoutId(): Int = R.layout.activity_create_address_result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                finish()
            }
            createAddressButton.onClick {
                // If there is no generated mailbox passphrase here, then we should revert the whole process to the
                // login state.
                viewModel.upgradeAccount(sessionId, username, domain, user.passphrase!!)
            }
            if (externalEmail == null) {
                // this means we are upgrading username-only account
                viewModel.domainsState.observeData {
                    if (it is AvailableDomains.State.Success) {
                        titleText.text =
                            String.format(
                                getString(
                                    R.string.auth_create_address_result_title_username,
                                    username,
                                    it.firstOrDefault
                                )
                            )
                    }
                }
                resultText.visibility = View.GONE
                externalEmailText.visibility = View.GONE
            } else {
                externalEmailText.text = externalEmail
                titleText.text = String.format(getString(R.string.auth_create_address_result_title), username)
            }
            termsConditionsText.movementMethod = LinkMovementMethod.getInstance()
        }

        viewModel.externalAccountUpgradeState.observeData(::onExternalAccountResultState)
    }

    private fun onExternalAccountResultState(state: UpdateExternalAccount.State) {
        when (state) {
            is UpdateExternalAccount.State.Processing -> showLoading(true)
            is UpdateExternalAccount.State.Error.Message -> showError(state.message)
            is UpdateExternalAccount.State.Success -> {
                onSuccess(user.copy(addresses = AddressesResult.from(state.address)))
            }
            is UpdateExternalAccount.State.Error.EmptyCredentials -> showError(
                getString(R.string.auth_create_address_error_credentials)
            )
            is UpdateExternalAccount.State.Error.EmptyDomain -> showError(
                getString(R.string.auth_create_address_error_no_available_domain)
            )
            is UpdateExternalAccount.State.Error.SetUsernameFailed -> showError(
                getString(R.string.auth_create_address_error_setusername)
            )
            is UpdateExternalAccount.State.Error.GeneratingPrivateKeyFailed -> showError(
                getString(R.string.auth_create_address_error_private_key)
            )
            is UpdateExternalAccount.State.Error.GeneratingSignedKeyListFailed -> showError(
                getString(R.string.auth_create_address_error_signed_key_list)
            )
        }
    }

    private fun onUsernameOnlyResultState(state: UpdateUsernameOnlyAccount.State) {
        when (state) {
            is UpdateUsernameOnlyAccount.State.Processing -> showLoading(true)
            is UpdateUsernameOnlyAccount.State.Error.Message -> showError(state.message)
            is UpdateUsernameOnlyAccount.State.Success -> onSuccess(UserResult.from(state.user))
            is UpdateUsernameOnlyAccount.State.Error.EmptyCredentials -> showError(
                getString(R.string.auth_create_address_error_credentials)
            )
            is UpdateUsernameOnlyAccount.State.Error.EmptyDomain -> showError(
                getString(R.string.auth_create_address_error_no_available_domain)
            )
            is UpdateUsernameOnlyAccount.State.Error.GeneratingPrivateKeyFailed -> showError(
                getString(R.string.auth_create_address_error_private_key)
            )
            is UpdateUsernameOnlyAccount.State.Error.GeneratingSignedKeyListFailed -> showError(
                getString(R.string.auth_create_address_error_signed_key_list)
            )
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

    /**
     * Invoked on successful completed mailbox login operation.
     */
    private fun onSuccess(user: UserResult) {
        val intent = Intent().apply { putExtra(ARG_USER_RESULT, user) }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_SESSION_ID = "arg.sessionId"
        const val ARG_USERNAME = "arg.username"
        const val ARG_EXTERNAL_EMAIL = "arg.externalEmail"
        const val ARG_DOMAIN = "arg.domain"
        const val ARG_USER = "arg.user"

        const val ARG_USER_RESULT = "arg.userResult"
    }
}
