package me.proton.core.humanverification.presentation.utils

import androidx.fragment.app.FragmentManager
import me.proton.android.core.presentation.utils.inTransaction
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationCaptcha
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialog
import me.proton.core.humanverification.presentation.ui.verification.CountryPickerFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationEmail
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationSMS

/**
 * Created by dinokadrikj on 6/3/20.
 */

private const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"

/**
 * Shows the human verification dialog.
 */
// TODO: dino SMS verification method is default on mail app, please double check
fun FragmentManager.showHumanVerification(
    availableVerificationMethods: List<String> = listOf("email", "captcha", "sms"), // SMS is by default
    containerId: Int = android.R.id.content,
    largeLayout: Boolean
) {

    val newFragment = HumanVerificationDialog(availableVerificationMethods)
    if (largeLayout) {
        // For large screens (tablets), we show the fragment as a dialog
        newFragment.show(this, TAG_HUMAN_VERIFICATION_DIALOG)
    } else {
        // The smaller screens (phones), we show the fragment fullscreen
        inTransaction {
            add(containerId, newFragment)
            addToBackStack(null)
        }
    }
}


/**
 * Client should supply the host. Originally it should be a simple operation extracted from the working
 * domain (host = URL(Constants.ENDPOINT_URI).host) or from the current active proxy URL when DoH is
 * enabled.
 */
internal fun FragmentManager.showHumanVerificationCaptchaContent(containerId: Int = android.R.id.content, token: String = "signup", host: String = "api.protonmail.ch") {
    val captchaFragment =
        HumanVerificationCaptcha(token, host)
    inTransaction {
        replace(containerId, captchaFragment)
    }
}

internal fun FragmentManager.showHumanVerificationEmailContent(containerId: Int = android.R.id.content, token: String = "signup") {
    val emailFragment =
        HumanVerificationEmail(token)
    inTransaction {
        replace(containerId, emailFragment)
    }
}

internal fun FragmentManager.showHumanVerificationSMSContent(containerId: Int = android.R.id.content, token: String = "signup") {
    val smsFragment =
        HumanVerificationSMS(token)
    inTransaction {
        replace(containerId, smsFragment)
    }
}

internal fun FragmentManager.showCountryPicker(containerId: Int = android.R.id.content) {
    val countryPickerFragment = CountryPickerFragment()
    inTransaction {
        add(containerId, countryPickerFragment)
    }
}


