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

package me.proton.core.humanverification.presentation.viewmodel.verification

import me.proton.core.country.presentation.entity.CountryUIModel
import studio.forface.viewstatestore.LockedViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * Interface that exposes 2 LiveData properties which are needed to avoid code duplication in the
 * Verification method fragments.
 *
 * @author Dino Kadrikj.
 */
internal interface HumanVerificationCode : ViewStateStoreScope {

    /**
     * Validation LiveData.
     */
    val validation: LockedViewStateStore<List<CountryUIModel>>

    /**
     * Verification code sending result LiveData.
     */
    val verificationCodeStatus: LockedViewStateStore<Boolean>
}
