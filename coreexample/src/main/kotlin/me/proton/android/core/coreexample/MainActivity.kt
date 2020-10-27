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
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.ui.CustomViewsActivity
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId

@AndroidEntryPoint
class MainActivity : ProtonActivity<ActivityMainBinding>() {

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
    }
}
