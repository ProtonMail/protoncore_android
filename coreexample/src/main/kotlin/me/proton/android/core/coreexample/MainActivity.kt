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

package me.proton.android.core.coreexample

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountAdded
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountInitializing
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.accountmanager.presentation.onAccountRemoved
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onSessionAuthenticated
import me.proton.core.accountmanager.presentation.onSessionForceLogout
import me.proton.core.accountmanager.presentation.onSessionHumanVerificationFailed
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ProtonActivity<ActivityMainBinding>() {

    @Inject
    lateinit var accountManager: AccountManager

    override fun layoutId(): Int = R.layout.activity_main

    private val authWorkflowLauncher = AuthOrchestrator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authWorkflowLauncher.register(this)

        binding.humanVerification.onClick {
            authWorkflowLauncher.startHumanVerificationWorkflow(
                SessionId("sessionId"),
                HumanVerificationDetails(
                    listOf(
                        VerificationMethod.CAPTCHA,
                        VerificationMethod.EMAIL,
                        VerificationMethod.PHONE
                    )
                )
            )
        }

        binding.customViews.onClick {
            startActivity(Intent(this, CustomViewsActivity::class.java))
        }

        binding.login.onClick {
            authWorkflowLauncher.startLoginWorkflow()
        }

        accountManager.getPrimaryUserId()
            .onEach { userId ->
                if (userId == null) {
                    val userId = UserId(UUID.randomUUID().toString())
                    val sessionId = SessionId(UUID.randomUUID().toString())
                    val session = Session(
                        sessionId = sessionId,
                        accessToken = "accessToken",
                        refreshToken = "refreshToken",
                        headers = null,
                        scopes = listOf()
                    )
                    accountManager.addAccount(
                        Account(
                            userId,
                            "username",
                            "example@example.com",
                            AccountState.Ready,
                            sessionId,
                            SessionState.Authenticated
                        ),
                        session
                    )
                }
            }.launchIn(lifecycleScope)

        accountManager.getSessions().onEach { }.launchIn(lifecycleScope)

        accountManager.observe(lifecycleScope)
            .onAccountAdded { }
            .onAccountDisabled { }
            .onAccountInitializing { }
            .onAccountReady { }
            .onAccountRemoved { }
            .onAccountTwoPassModeFailed { }
            .onSessionAuthenticated { }
            .onSessionForceLogout { }
            .onSessionHumanVerificationFailed { }

        accountManager.onHumanVerificationNeeded().onEach { (account, details) ->
            account.sessionId?.let {
                authWorkflowLauncher.startHumanVerificationWorkflow(it, details)
            }
        }.launchIn(lifecycleScope)
    }
}
