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
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import me.proton.android.core.presentation.utils.inTransaction
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.ui.HumanVerificationHelpFragment
import me.proton.core.humanverification.presentation.ui.verification.CountryPickerFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationCaptchaFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationEmailFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationEnterCodeFragment
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationSMSFragment

/**
 * @author Dino Kadrikj.
 */
private const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"
private const val TAG_HUMAN_VERIFICATION_COUNTRY_PICKER = "human_verification_country"
private const val TAG_HUMAN_VERIFICATION_ENTER_CODE = "human_verification_code"
const val TAG_HUMAN_VERIFICATION_HELP = "human_verification_help"

const val TOKEN_DEFAULT = "signup"
const val HOST_DEFAULT = "api.protonmail.ch"

val defaultVerificationMethods = listOf(
    TokenType.CAPTCHA.tokenTypeValue,
    TokenType.EMAIL.tokenTypeValue,
    TokenType.SMS.tokenTypeValue
)

/** Shows the human verification dialog. */
fun FragmentManager.showHumanVerification(
    availableVerificationMethods: List<String> = defaultVerificationMethods,
    captchaToken: String? = null,
    largeLayout: Boolean
) {

    val newFragment = HumanVerificationDialogFragment(availableVerificationMethods, captchaToken)
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
    token: String?,
    host: String = HOST_DEFAULT
): Fragment {
    val captchaFragment = HumanVerificationCaptchaFragment(token ?: TOKEN_DEFAULT, host)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, captchaFragment)
    }
    return captchaFragment
}

internal fun FragmentManager.showHumanVerificationEmailContent(
    containerId: Int = android.R.id.content,
    token: String = TOKEN_DEFAULT
) {
    val emailFragment = HumanVerificationEmailFragment(token)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, emailFragment)
    }
}

internal fun FragmentManager.showHumanVerificationSMSContent(
    containerId: Int = android.R.id.content,
    token: String = TOKEN_DEFAULT
) {
    val smsFragment = HumanVerificationSMSFragment(token)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, smsFragment)
    }
}

internal fun FragmentManager.showEnterCode(
    tokenType: TokenType,
    destination: String?
) {
    val enterCodeFragment = HumanVerificationEnterCodeFragment(tokenType, destination)
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

internal fun FragmentManager.showCountryPicker() {
    val countryPickerFragment = CountryPickerFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(countryPickerFragment, TAG_HUMAN_VERIFICATION_COUNTRY_PICKER)
    }
}

internal fun CountryPickerFragment.removeCountryPicker() {
    parentFragmentManager.inTransaction {
        remove(this@removeCountryPicker)
    }
}

internal fun Context.openBrowserLink(link: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    intent.resolveActivity(packageManager)?.let {
        startActivity(intent)
    } ?: run {
        Toast.makeText(
            this,
            getString(R.string.human_verification_browser_missing),
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun View.errorSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_error)
}

internal fun View.successSnack(@StringRes messageRes: Int) {
    snack(messageRes = messageRes, color = R.drawable.background_success)
}

private fun View.snack(
    @StringRes messageRes: Int,
    @DrawableRes color: Int
) {
    snack(message = resources.getString(messageRes), color = color)
}

private fun View.snack(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    @DrawableRes color: Int
) {
    Snackbar.make(this, message, length).apply {
        view.background = context.resources.getDrawable(color, null)
        setTextColor(context.resources.getColor(R.color.text_light))
    }.show()
}

fun EditText.setInputError() = apply {
    background = resources.getDrawable(R.drawable.default_edit_text_error, null)
}

fun EditText.removeInputError() = apply {
    background = resources.getDrawable(R.drawable.default_edit_text, null)
}
