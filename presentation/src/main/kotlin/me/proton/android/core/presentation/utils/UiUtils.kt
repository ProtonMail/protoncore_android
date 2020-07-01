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

package me.proton.android.core.presentation.utils

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import me.proton.android.core.presentation.ui.HumanVerificationDialog

/**
 * Created by dinokadrikj on 5/15/20.
 */

private const val TAG_HUMAN_VERIFICATION_DIALOG = "human_verification_dialog"

/**
 * Shows the human verification dialog.
 */
fun FragmentManager.showHumanVerification(largeLayout: Boolean, containerId: Int = android.R.id.content) {
    val newFragment = HumanVerificationDialog()
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
 *
 */
inline fun FragmentManager.inTransaction(block: FragmentTransaction.() -> FragmentTransaction) {
    val transaction = beginTransaction()
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    transaction.block()
    transaction.commit()
}