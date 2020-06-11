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

import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * Created by dinokadrikj on 5/25/20.
 */
class HumanVerificationViewModel(private val availableVerificationMethods: List<String>) : ProtonViewModel(), ViewStateStoreScope {
    // TODO: dino currently working with strings,change later to enums or something else typesafety
    private lateinit var currentActiveVerificationMethod: String

    val activeMethod = ViewStateStore<String>()
    val enabledMethods = ViewStateStore<List<String>>().lock

    init {
        enabledMethods.set(availableVerificationMethods, true)
        defineActiveVerificationMethod()
    }

    fun defineActiveVerificationMethod(userSelectedMethod: String? = null) {
        userSelectedMethod?.let {
            currentActiveVerificationMethod = it
        } ?: run {
            currentActiveVerificationMethod = availableVerificationMethods.sorted()[0]
        }
        activeMethod.set(currentActiveVerificationMethod, false)
    }

    /**
     * Submits the human verification token to the API for every verification method supported
     * (Captcha, Email and SMS).
     */
    fun submitHumanVerificationToken(token: String) {

    }
}
