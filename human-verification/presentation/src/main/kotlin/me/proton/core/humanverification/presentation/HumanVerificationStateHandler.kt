/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.humanverification.presentation

import android.app.Activity
import android.content.Intent
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.ui.HumanVerificationActivity
import me.proton.core.network.domain.client.getType
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumanVerificationStateHandler @Inject constructor(
    private val activityProvider: ActivityProvider,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val humanVerificationManager: HumanVerificationManager
) {
    fun observe() {
        humanVerificationManager
            .observe(appLifecycleObserver.lifecycle)
            .onHumanVerificationNeeded { hvDetails ->
                activityProvider.lastResumed?.let { activity ->
                    startHumanVerificationWorkflow(activity, hvDetails.toHvInput())
                }
            }
    }

    private fun startHumanVerificationWorkflow(activity: Activity, input: HumanVerificationInput) {
        val intent = Intent(activity, HumanVerificationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(HumanVerificationActivity.ARG_INPUT, input)
        }
        activity.startActivityForResult(intent, 0)
    }
}

internal fun HumanVerificationDetails.toHvInput() = HumanVerificationInput(
    clientId = clientId.id,
    clientIdType = clientId.getType().value,
    verificationMethods = verificationMethods,
    verificationToken = requireNotNull(verificationToken)
)
