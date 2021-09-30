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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.HumanVerificationHelpFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationCaptchaFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationEmailFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationEnterCodeFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationSMSFragment
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog
import me.proton.core.presentation.utils.inTransaction

private const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"
private const val TAG_HUMAN_VERIFICATION_ENTER_CODE = "human_verification_code"
private const val TAG_HUMAN_VERIFICATION_NEW_CODE_DIALOG = "human_verification_new_code_dialog"
const val TAG_HUMAN_VERIFICATION_HELP = "human_verification_help"

const val TOKEN_DEFAULT = "signup"

val defaultVerificationMethods = listOf(
    TokenType.CAPTCHA.value,
    TokenType.EMAIL.value,
    TokenType.SMS.value
)

/** Shows the human verification dialog. */
fun FragmentManager.showHumanVerification(
    clientId: String,
    captchaUrl: String? = null,
    clientIdType: String,
    availableVerificationMethods: List<String> = defaultVerificationMethods,
    captchaToken: String? = null,
    recoveryEmailAddress: String? = null,
    largeLayout: Boolean
) {
    if (findFragmentByTag(TAG_HUMAN_VERIFICATION_DIALOG) != null) return

    val newFragment = HumanVerificationDialogFragment(
        clientId = clientId,
        captchaUrl = captchaUrl,
        clientIdType = clientIdType,
        availableVerificationMethods = availableVerificationMethods,
        captchaToken = captchaToken,
        recoveryEmailAddress = recoveryEmailAddress
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

/**
 * Client should supply the host. Originally it should be a simple operation extracted from the
 * working domain (host = URL(Constants.ENDPOINT_URI).host) or from the current active proxy URL
 * when DoH is enabled.
 */
internal fun FragmentManager.showHumanVerificationCaptchaContent(
    containerId: Int = android.R.id.content,
    captchaUrl: String? = null,
    token: String? = null
): Fragment {
    val captchaFragment = HumanVerificationCaptchaFragment(
        captchaUrl = captchaUrl, urlToken = token ?: TOKEN_DEFAULT
    )
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, captchaFragment)
    }
    return captchaFragment
}

internal fun FragmentManager.showHumanVerificationEmailContent(
    containerId: Int = android.R.id.content,
    sessionId: SessionId?,
    token: String = TOKEN_DEFAULT,
    recoveryEmailAddress: String? = null
) {
    val emailFragment = HumanVerificationEmailFragment(sessionId?.id, token, recoveryEmailAddress)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, emailFragment)
    }
}

internal fun FragmentManager.showHumanVerificationSMSContent(
    sessionId: SessionId?,
    containerId: Int = android.R.id.content,
    token: String = TOKEN_DEFAULT
) {
    val smsFragment = HumanVerificationSMSFragment(sessionId?.id, token)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, smsFragment)
    }
}

internal fun FragmentManager.showEnterCode(
    sessionId: SessionId?,
    tokenType: TokenType,
    destination: String?
) {
    val enterCodeFragment = HumanVerificationEnterCodeFragment(sessionId?.id, tokenType, destination)
    inTransaction {
        setCustomAnimations(0, 0)
        add(enterCodeFragment, TAG_HUMAN_VERIFICATION_ENTER_CODE)
    }
}

internal fun FragmentManager.showHelp() {
    val helpFragment = HumanVerificationHelpFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(helpFragment, TAG_HUMAN_VERIFICATION_HELP)
    }
}

/**
 * Presents to the user a dialog to confirm that it really wants a replacement code.
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showRequestNewCodeDialog(
    context: Context,
    destination: String? = "",
    largeLayout: Boolean = false
) {
    findFragmentByTag(TAG_HUMAN_VERIFICATION_NEW_CODE_DIALOG) ?: run {
        val dialogFragment = ProtonCancellableAlertDialog(
            title = context.getString(R.string.human_verification_code_request_new_code_title),
            description = String.format(
                context.getString(R.string.human_verification_code_request_new_code),
                destination
            ),
            positiveButton = context.getString(R.string.human_verification_code_request_new_code_action)
        )
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            dialogFragment.show(this, TAG_HUMAN_VERIFICATION_NEW_CODE_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(dialogFragment, TAG_HUMAN_VERIFICATION_NEW_CODE_DIALOG)
            }
        }
    }
}
