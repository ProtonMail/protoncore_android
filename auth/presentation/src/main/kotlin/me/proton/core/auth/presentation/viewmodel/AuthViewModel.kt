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

package me.proton.core.auth.presentation.viewmodel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.humanverification.presentation.observe
import me.proton.core.humanverification.presentation.onHumanVerificationNeeded
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.presentation.viewmodel.ProtonViewModel

internal abstract class AuthViewModel(
    private val humanVerificationManager: HumanVerificationManager,
    private val humanVerificationOrchestrator: HumanVerificationOrchestrator
) : ProtonViewModel() {

    fun register(context: ComponentActivity) {
        humanVerificationOrchestrator.register(context)
    }

    abstract val recoveryEmailAddress: String?

    protected fun handleHumanVerificationState(context: ComponentActivity) =
        humanVerificationManager.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
            .onHumanVerificationNeeded {
                humanVerificationOrchestrator.startHumanVerificationSignUpWorkflow(
                    clientId = it.clientId,
                    details = HumanVerificationApiDetails(
                        it.verificationMethods, it.captchaVerificationToken
                    ),
                    recoveryEmailAddress = recoveryEmailAddress
                )
            }
}
