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

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.auth.presentation.databinding.ActivityAuthHelpBinding
import me.proton.core.auth.presentation.entity.AuthHelpInput
import me.proton.core.auth.presentation.entity.AuthHelpResult
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.devicemigration.presentation.StartMigrationFromTargetDevice
import me.proton.core.devicemigration.presentation.TargetDeviceMigrationResult
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import me.proton.core.presentation.utils.onClick
import javax.inject.Inject

@AndroidEntryPoint
class AuthHelpActivity : AuthActivity<ActivityAuthHelpBinding>(ActivityAuthHelpBinding::inflate) {

    @Inject
    lateinit var helpOptionHandler: HelpOptionHandler

    @Inject
    lateinit var isEasyDeviceMigrationAvailable: IsEasyDeviceMigrationAvailable

    private val input: AuthHelpInput by lazy {
        requireNotNull(intent.getParcelableExtra(ARG_INPUT))
    }

    private lateinit var targetDeviceMigrationLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)

        targetDeviceMigrationLauncher =
            registerForActivityResult(StartMigrationFromTargetDevice(), this::onSignedInResult)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            lifecycleScope.launch {
                helpOptionSignInWithQrCode.root.isVisible =
                    input.shouldShowQrLogin && isEasyDeviceMigrationAvailable(userId = null)
            }
            helpOptionSignInWithQrCode.root.onClick {
                targetDeviceMigrationLauncher.launch(Unit)
            }

            helpOptionCustomerSupport.root.onClick {
                helpOptionHandler.onCustomerSupport(this@AuthHelpActivity)
            }

            helpOptionOtherIssues.root.onClick {
                helpOptionHandler.onOtherSignInIssues(this@AuthHelpActivity)
            }
            helpOptionForgotPassword.root.onClick {
                helpOptionHandler.onForgotPassword(this@AuthHelpActivity)
            }
            helpOptionForgotUsername.root.onClick {
                helpOptionHandler.onForgotUsername(this@AuthHelpActivity)
            }
        }
    }

    private fun onSignedInResult(result: TargetDeviceMigrationResult?) {
        when (result) {
            is TargetDeviceMigrationResult.NavigateToSignIn -> {
                setResult(RESULT_CANCELED)
                finish()
            }

            is TargetDeviceMigrationResult.SignedIn -> {
                setOkResult(AuthHelpResult.SignedInWithEdm(result.userId))
                finish()
            }

            is TargetDeviceMigrationResult.PasswordChangeNeeded -> {
                setOkResult(AuthHelpResult.PasswordChangeNeededAfterEdm)
                finish()
            }

            null -> Unit
        }
    }

    private fun setOkResult(result: AuthHelpResult) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ARG_RESULT, result)
        })
    }

    companion object {
        const val ARG_INPUT = "input"
        const val ARG_RESULT = "result"
    }
}
