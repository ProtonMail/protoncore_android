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

package me.proton.core.humanverification.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.ActivityHumanVerificationBinding
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.utils.defaultVerificationMethods
import me.proton.core.humanverification.presentation.utils.showHumanVerification
import me.proton.core.presentation.ui.ProtonActivity

/**
 * Activity that "wraps" and handles the whole Human Verification process.
 */
@AndroidEntryPoint
class HumanVerificationActivity :
    ProtonActivity<ActivityHumanVerificationBinding>(),
    HumanVerificationDialogFragment.OnResultListener {

    override fun layoutId(): Int = R.layout.activity_human_verification

    private val input: HumanVerificationInput by lazy {
        requireNotNull(intent?.getParcelableExtra(ARG_HUMAN_VERIFICATION_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.showHumanVerification(
            clientId = input.clientId,
            captchaBaseUrl = input.captchaBaseUrl,
            clientIdType = input.clientIdType,
            // filter only the app supported verification methods. (the API can send more of them).
            availableVerificationMethods = input.verificationMethods?.filter { defaultVerificationMethods.contains(it) }
                ?: defaultVerificationMethods,
            captchaToken = input.captchaToken,
            largeLayout = false,
            recoveryEmailAddress = input.recoveryEmailAddress
        )
    }

    override fun setResult(result: HumanVerificationResult?) {
        result?.let {
            val intent = Intent().apply { putExtra(ARG_HUMAN_VERIFICATION_RESULT, it) }
            setResult(Activity.RESULT_OK, intent)
        } ?: run {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    companion object {
        const val ARG_HUMAN_VERIFICATION_INPUT = "arg.humanVerificationInput"
        const val ARG_HUMAN_VERIFICATION_RESULT = "arg.humanVerificationResult"
    }
}
