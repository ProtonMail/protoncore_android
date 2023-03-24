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

package me.proton.core.auth.presentation.ui

import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.ui.signup.SignupFinishedFragment
import me.proton.core.auth.presentation.ui.signup.CreatingUserFragment
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.ui.alert.ProtonCancellableAlertDialog
import me.proton.core.presentation.utils.inTransaction
import me.proton.core.presentation.utils.openBrowserLink

private const val TAG_PASSWORD_CHOOSER_DIALOG = "password_chooser_dialog"
private const val TAG_CREATING_USER = "creating_user_fragment"
private const val TAG_CONGRATS = "congrats_fragment"

/**
 * Presents to the user a dialog to inform that it should update the password inn order to be able to login.
 * The positive response should bring the user to a web browser.
 *
 * @param largeLayout how to present the dialog (default false)
 */
fun FragmentManager.showPasswordChangeDialog(
    context: ComponentActivity,
    largeLayout: Boolean = false
) {
    findFragmentByTag(TAG_PASSWORD_CHOOSER_DIALOG) ?: run {
        val updateDialogFragment = ProtonCancellableAlertDialog(
            context.getString(R.string.auth_password_chooser_title),
            context.getString(R.string.auth_password_chooser_description),
            context.getString(R.string.auth_password_chooser_change)
        )
        setFragmentResultListener(
            ProtonCancellableAlertDialog.KEY_ACTION_DONE,
            context
        ) { _, _ ->
            context.openBrowserLink(context.getString(R.string.login_link))
        }
        if (largeLayout) {
            // For large screens (tablets), we show the fragment as a dialog
            updateDialogFragment.show(this, TAG_PASSWORD_CHOOSER_DIALOG)
        } else {
            // The smaller screens (phones), we show the fragment fullscreen
            inTransaction {
                add(updateDialogFragment, TAG_PASSWORD_CHOOSER_DIALOG)
            }
        }
    }
}

fun FragmentManager.showCreatingUser(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_CREATING_USER) ?: run {
    val creatingUserFragment = CreatingUserFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        add(containerId, creatingUserFragment, TAG_CREATING_USER)
    }
    creatingUserFragment
}

fun FragmentManager.removeCreatingUser() {
    findFragmentByTag(TAG_CREATING_USER)?.let {
        inTransaction {
            setCustomAnimations(0, 0)
            remove(it)
        }
    }
}

fun FragmentManager.showCongrats(
    containerId: Int = android.R.id.content
) = findFragmentByTag(TAG_CONGRATS) ?: run {
    val congratsFragment = SignupFinishedFragment()
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, congratsFragment, TAG_CONGRATS)
    }
    congratsFragment
}

fun FragmentManager.removeCongrats() {
    findFragmentByTag(TAG_CONGRATS)?.let {
        inTransaction {
            setCustomAnimations(0, 0)
            remove(it)
        }
        popBackStack()
    }
}

/**
 * Helper method to monitor flow for state staying as one for prolonged period of time to trigger specific actions
 */
internal inline fun <E> Flow<Any>.onLongState(state: E, crossinline action: () -> Unit) = flatMapLatest {
    if (it == state) flow {
        delay(10000)
        emit(Unit)
    }
    else emptyFlow()
}.onEach {
    action()
}
