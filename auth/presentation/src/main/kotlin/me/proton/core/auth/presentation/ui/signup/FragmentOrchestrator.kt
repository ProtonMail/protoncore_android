/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui.signup

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.entity.signup.SubscriptionDetails
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog
import me.proton.core.presentation.utils.inTransaction

private const val TAG_USERNAME_CHOOSER = "username_chooser_fragment"
private const val TAG_INTERNAL_EMAIL_CHOOSER = "internal_email_chooser_fragment"
private const val TAG_EXTERNAL_EMAIL_CHOOSER = "external_email_chooser_fragment"
private const val TAG_PASSWORD_CHOOSER = "password_chooser_fragment"
private const val TAG_RECOVERY_CHOOSER = "recovery_chooser_fragment"
private const val TAG_SKIP_RECOVERY_DIALOG = "skip_recovery_dialog"
private const val TAG_EMAIL_RECOVERY_FRAGMENT = "email_recovery_fragment"
private const val TAG_SMS_RECOVERY_FRAGMENT = "skip_recovery_fragment"
private const val TAG_TERMS_CONDITIONS_FRAGMENT = "terms_conditions_fragment"
private const val TAG_PRIVACY_POLICY_FRAGMENT = "privacy_policy_fragment"

fun FragmentManager.registerSkipRecoveryDialogResultLauncher(
    fragment: Fragment,
    onResult: () -> Unit
): FragmentDialogResultLauncher<Unit> {
    setFragmentResultListener(ProtonCancellableAlertDialog.KEY_ACTION_DONE, fragment) { _, _ ->
        onResult()
    }
    return FragmentDialogResultLauncher(
        requestKey = ProtonCancellableAlertDialog.KEY_ACTION_DONE,
        show = { showSkipRecoveryDialog(fragment.requireContext()) }
    )
}

internal fun FragmentManager.showEmailRecoveryMethodFragment(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_EMAIL_RECOVERY_FRAGMENT) ?: run {
    val emailRecoveryFragment = RecoveryEmailFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, emailRecoveryFragment, TAG_EMAIL_RECOVERY_FRAGMENT)
    }
    emailRecoveryFragment
}

internal fun FragmentManager.showSMSRecoveryMethodFragment(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_SMS_RECOVERY_FRAGMENT) ?: run {
    val smsRecoveryFragment = RecoverySMSFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, smsRecoveryFragment, TAG_SMS_RECOVERY_FRAGMENT)
    }
    smsRecoveryFragment
}

internal fun FragmentManager.showTermsConditions() {
    val termsConditionsDialogFragment = TermsConditionsDialogFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(termsConditionsDialogFragment, TAG_TERMS_CONDITIONS_FRAGMENT)
        addToBackStack(TAG_TERMS_CONDITIONS_FRAGMENT)
    }
}

internal fun FragmentManager.showPrivacyPolicy() {
    val termsConditionsDialogFragment = PrivacyPolicyDialogFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(termsConditionsDialogFragment, TAG_PRIVACY_POLICY_FRAGMENT)
        addToBackStack(TAG_PRIVACY_POLICY_FRAGMENT)
    }
}

internal fun FragmentManager.showUsernameChooser(
    cancellable: Boolean = true,
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_USERNAME_CHOOSER) ?: run {
    val fragment = ChooseUsernameFragment(cancellable)
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, fragment, TAG_USERNAME_CHOOSER)
    }
    fragment
}

internal fun FragmentManager.showInternalEmailChooser(
    creatableAccountType: AccountType,
    cancellable: Boolean = true,
    username: String? = null,
    domain: String? = null,
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_INTERNAL_EMAIL_CHOOSER) ?: run {
    val fragment = ChooseInternalEmailFragment(creatableAccountType, cancellable, username, domain)
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, fragment, TAG_INTERNAL_EMAIL_CHOOSER)
    }
    fragment
}

internal fun FragmentManager.showExternalEmailChooser(
    creatableAccountType: AccountType,
    cancellable: Boolean = true,
    email: String? = null,
    subscriptionDetails: SubscriptionDetails? = null,
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_EXTERNAL_EMAIL_CHOOSER) ?: run {
    val fragment = ChooseExternalEmailFragment(creatableAccountType, cancellable, email, subscriptionDetails)
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, fragment, TAG_EXTERNAL_EMAIL_CHOOSER)
    }
    fragment
}

internal fun FragmentManager.replaceByInternalEmailChooser(
    creatableAccountType: AccountType,
    cancellable: Boolean = true,
    username: String? = null,
    domain: String? = null,
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_INTERNAL_EMAIL_CHOOSER) ?: run {
    val fragment = ChooseInternalEmailFragment(creatableAccountType, cancellable, username, domain)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, fragment, TAG_INTERNAL_EMAIL_CHOOSER)
    }
    fragment
}

internal fun FragmentManager.replaceByExternalEmailChooser(
    creatableAccountType: AccountType,
    cancellable: Boolean = true,
    email: String? = null,
    subscriptionDetails: SubscriptionDetails? = null,
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_EXTERNAL_EMAIL_CHOOSER) ?: run {
    val fragment = ChooseExternalEmailFragment(creatableAccountType, cancellable, email, subscriptionDetails)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, fragment, TAG_EXTERNAL_EMAIL_CHOOSER)
    }
    fragment
}

internal fun FragmentManager.showPasswordChooser(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_PASSWORD_CHOOSER) ?: run {
    val chooserPasswordFragment = ChoosePasswordFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, chooserPasswordFragment, TAG_PASSWORD_CHOOSER)
        addToBackStack(TAG_PASSWORD_CHOOSER)
    }
    chooserPasswordFragment
}

internal fun FragmentManager.showRecoveryMethodChooser(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_RECOVERY_CHOOSER) ?: run {
    val recoveryMethodFragment = RecoveryMethodFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, recoveryMethodFragment, TAG_RECOVERY_CHOOSER)
        addToBackStack(TAG_RECOVERY_CHOOSER)
    }
    recoveryMethodFragment
}

internal fun FragmentManager.showSkipRecoveryDialog(
    context: Context
) {
    findFragmentByTag(TAG_SKIP_RECOVERY_DIALOG) ?: run {
        val updateDialogFragment = ProtonCancellableAlertDialog(
            title = context.getString(R.string.auth_signup_skip_recovery_title),
            description = context.getString(R.string.auth_signup_skip_recovery_description),
            positiveButton = context.getString(R.string.auth_signup_skip_recovery),
            negativeButton = context.getString(R.string.auth_signup_set_recovery)
        )
        updateDialogFragment.show(this, TAG_SKIP_RECOVERY_DIALOG)
    }
}
