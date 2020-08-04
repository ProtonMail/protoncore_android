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
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.ActivityHumanVerificationBinding
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.ARG_TOKEN_CODE
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment.Companion.ARG_TOKEN_TYPE
import me.proton.core.humanverification.presentation.utils.defaultVerificationMethods
import me.proton.core.humanverification.presentation.utils.showHumanVerification

/**
 * Activity that "wraps" and handles the whole Human Verification process.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class HumanVerificationActivity : ProtonActivity<ActivityHumanVerificationBinding>(),
    HumanVerificationDialogFragment.OnResultListener {

    companion object {
        const val ARG_VERIFICATION_OPTIONS = "arg.verification-options"
        const val ARG_CAPTCHA_TOKEN = "arg.captcha-token"
    }

    override fun layoutId(): Int = R.layout.activity_human_verification

    private val verificationMethods: List<String>? by lazy {
        intent?.extras?.get(ARG_VERIFICATION_OPTIONS) as List<String>?
    }

    private val captchaToken: String? by lazy {
        intent?.extras?.getString(ARG_VERIFICATION_OPTIONS, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.showHumanVerification(
            availableVerificationMethods = verificationMethods ?: defaultVerificationMethods,
            captchaToken = captchaToken,
            largeLayout = false
        )
    }

    override fun setResult(result: HumanVerificationResult) {
        val intent = Intent()
        intent.putExtras(
            bundleOf(
                ARG_TOKEN_CODE to result.tokenCode,
                ARG_TOKEN_TYPE to result.tokenType
            )
        )
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
