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

package me.proton.core.humanverification.presentation.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.HumanVerificationActivity
import me.proton.core.network.domain.UserData
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationHeaders
import javax.inject.Inject

/**
 * Utils class to help clients handle the human verification start-receive process.
 * Clients can use their implementation of course according to their needs.
 * @author Dino Kadrikj.
 */
class HumanVerificationBinder @Inject constructor(
    private val context: Context,
    private val channel: Channel<HumanVerificationResult>,
    private val userData: UserData
) {
    /**
     * Starts the Human Verification UI and listens for the chanel result.
     * @param humanVerificationDetails the details object returned from the API which holds the available methods
     * and eventual captcha token if that method is within the available list.
     */
    suspend operator fun invoke(humanVerificationDetails: HumanVerificationDetails): Boolean {
        // start the human verification activity here
        withContext(Dispatchers.Main) {
            ContextCompat.startActivity(
                context,
                Intent(context, HumanVerificationActivity::class.java),
                bundleOf(
                    HumanVerificationActivity.ARG_VERIFICATION_OPTIONS to
                        humanVerificationDetails.verificationMethods.map {
                            it.name
                        },
                    HumanVerificationActivity.ARG_CAPTCHA_TOKEN to humanVerificationDetails.captchaVerificationToken
                )
            )
        }

        val result = channel.receive()

        // the human verification result token and code are set to user data so that network can set them as headers
        // the client needs to store this for further API call because the BE can use multi-use tokens
        if (result.success) {
            // if the result is success, the token type and code values are always set
            userData.humanVerificationHandler =
                HumanVerificationHeaders(result.tokenType!!.tokenTypeValue, result.tokenCode!!)
        }
        return result.success
    }
}
