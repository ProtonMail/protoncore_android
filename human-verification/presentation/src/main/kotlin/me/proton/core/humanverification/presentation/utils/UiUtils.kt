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

import androidx.fragment.app.FragmentManager
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.ui.HumanVerificationHelpFragment
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.presentation.utils.inTransaction

private const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"
const val TAG_HUMAN_VERIFICATION_HELP = "human_verification_help"

/** Shows the human verification dialog. */
fun FragmentManager.showHumanVerification(
    clientId: String,
    captchaUrl: String? = null,
    clientIdType: String,
    availableVerificationMethods: List<String> = emptyList(),
    verificationToken: String,
    recoveryEmailAddress: String? = null,
    largeLayout: Boolean
) {
    if (findFragmentByTag(TAG_HUMAN_VERIFICATION_DIALOG) != null) return

    val newFragment = HumanVerificationDialogFragment(
        clientId = clientId,
        clientIdType = clientIdType,
        baseUrl = captchaUrl,
        verificationMethods = availableVerificationMethods,
        startToken = verificationToken,
        recoveryEmail = recoveryEmailAddress,
    )
    if (largeLayout) {
        // For large screens (tablets), we show the fragment as a dialog
        newFragment.show(this, TAG_HUMAN_VERIFICATION_DIALOG)
    } else {
        // The smaller screens (phones), we show the fragment fullscreen
        inTransaction {
            add(newFragment, TAG_HUMAN_VERIFICATION_DIALOG)
        }
    }
}

internal fun FragmentManager.showHelp() {
    val helpFragment = HumanVerificationHelpFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(helpFragment, TAG_HUMAN_VERIFICATION_HELP)
    }
}
