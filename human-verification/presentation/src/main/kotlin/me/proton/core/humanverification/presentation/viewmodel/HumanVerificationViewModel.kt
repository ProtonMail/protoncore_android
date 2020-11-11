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

package me.proton.core.humanverification.presentation.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.exception.NotEnoughVerificationOptions
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class to serve the main Human Verification screen.
 *
 * @author Dino Kadrikj.
 */
class HumanVerificationViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) :
    ProtonViewModel(), ViewStateStoreScope {

    private lateinit var currentActiveVerificationMethod: TokenType

    val activeMethod = ViewStateStore<String>().lock
    val enabledMethods = ViewStateStore<List<String>>().lock
    private var availableVerificationMethods: List<String> =
        savedStateHandle.get<List<String>>(HumanVerificationDialogFragment.ARG_VERIFICATION_OPTIONS)!!

    init {
        // A list of all available methods that the API is currently supporting for this particular user and device.
        // The UI should present the verification methods for each one of them.
        // It is safe to use !! here, guaranteed that there will be at least 1 verification method available
        if (availableVerificationMethods.isEmpty()) {
            throw NotEnoughVerificationOptions("Please provide at least 1 verification method")
        }

        enabledMethods.setData(data = availableVerificationMethods, dropOnSame = true)
        defineActiveVerificationMethod()
    }

    /**
     * Sets the currently active verification method that the user chose.
     */
    fun defineActiveVerificationMethod(userSelectedMethod: TokenType? = null) {
        userSelectedMethod?.let {
            currentActiveVerificationMethod = it
        } ?: run {
            currentActiveVerificationMethod =
                TokenType.fromString(availableVerificationMethods.sorted()[0])
        }
        activeMethod.setData(
            data = currentActiveVerificationMethod.tokenTypeValue,
            dropOnSame = false
        )
    }
}
