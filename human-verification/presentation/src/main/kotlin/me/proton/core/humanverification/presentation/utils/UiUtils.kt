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

package me.proton.core.humanverification.presentation.utils

import androidx.fragment.app.FragmentManager
import me.proton.core.humanverification.presentation.ui.common.HumanVerificationHelpFragment
import me.proton.core.humanverification.presentation.ui.hv3.HV3DialogFragment
import me.proton.core.presentation.utils.inTransaction

internal const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"
const val TAG_HUMAN_VERIFICATION_HELP = "human_verification_help"

/** Shows the human verification dialog. */
fun FragmentManager.showHumanVerification(
    humanVerificationVersion: HumanVerificationVersion,
    clientId: String,
    clientIdType: String,
    verificationToken: String,
    verificationMethods: List<String> = emptyList(),
    recoveryEmail: String? = null
) {
    if (findFragmentByTag(TAG_HUMAN_VERIFICATION_DIALOG) != null) return

    val newFragment = if (humanVerificationVersion == HumanVerificationVersion.HV3) {
        HV3DialogFragment(
            clientId = clientId,
            clientIdType = clientIdType,
            verificationMethods = verificationMethods,
            startToken = verificationToken,
            recoveryEmail = recoveryEmail
        )
    } else {
        error("Human Verification version $humanVerificationVersion is not supported.")
    }
    inTransaction {
        setCustomAnimations(0, 0)
        add(newFragment, TAG_HUMAN_VERIFICATION_DIALOG)
    }
}

fun FragmentManager.hasHumanVerificationFragment(): Boolean =
    findFragmentByTag(TAG_HUMAN_VERIFICATION_DIALOG) != null

internal fun FragmentManager.showHelp() {
    val helpFragment = HumanVerificationHelpFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(helpFragment, TAG_HUMAN_VERIFICATION_HELP)
    }
}
