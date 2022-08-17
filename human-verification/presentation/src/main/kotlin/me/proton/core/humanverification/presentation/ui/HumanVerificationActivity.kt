/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.humanverification.domain.HumanVerificationExternalInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.common.REQUEST_KEY
import me.proton.core.humanverification.presentation.ui.common.RESULT_HUMAN_VERIFICATION
import me.proton.core.humanverification.presentation.utils.HumanVerificationVersion
import me.proton.core.humanverification.presentation.utils.showHumanVerification
import javax.inject.Inject

@AndroidEntryPoint
class HumanVerificationActivity : FragmentActivity() {

    @Inject
    lateinit var humanVerificationVersion: HumanVerificationVersion

    @Inject
    lateinit var humanverificationExternalInput: HumanVerificationExternalInput

    private val input: HumanVerificationInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.setFragmentResultListener(REQUEST_KEY, this) { _, bundle ->
            val result = bundle.getParcelable<HumanVerificationResult>(RESULT_HUMAN_VERIFICATION)
            if (result != null) {
                val intent = Intent().putExtra(ARG_RESULT, result)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }

        supportFragmentManager.showHumanVerification(
            humanVerificationVersion = humanVerificationVersion,
            clientId = input.clientId,
            clientIdType = input.clientIdType,
            verificationMethods = input.verificationMethods,
            verificationToken = input.verificationToken,
            recoveryEmail = humanverificationExternalInput.recoveryEmail
        )
    }

    companion object {
        const val ARG_INPUT = "arg.hvInput"
        const val ARG_RESULT = "arg.hvResult"
    }
}
