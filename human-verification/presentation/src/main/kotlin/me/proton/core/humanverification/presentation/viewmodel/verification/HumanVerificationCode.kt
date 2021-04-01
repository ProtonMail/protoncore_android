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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.presentation.viewmodel.ViewModelResult

/**
 * Interface that exposes 2 LiveData properties which are needed to avoid code duplication in the
 * Verification method fragments.
 *
 * @author Dino Kadrikj.
 */
internal interface HumanVerificationCode {

    /**
     * Validation LiveData.
     */
    val validation: StateFlow<ViewModelResult<List<CountryUIModel>>>
        get() = getNewValidation().asStateFlow()

    /**
     * Verification code sending result LiveData.
     */
    val verificationCodeStatus: StateFlow<ViewModelResult<Boolean>>
        get() = getNewVerificationCodeStatus().asStateFlow()

    fun getNewValidation(): MutableStateFlow<ViewModelResult<List<CountryUIModel>>> = MutableStateFlow(ViewModelResult.None)
    fun getNewVerificationCodeStatus(): MutableStateFlow<ViewModelResult<Boolean>> = MutableStateFlow(ViewModelResult.None)

}
