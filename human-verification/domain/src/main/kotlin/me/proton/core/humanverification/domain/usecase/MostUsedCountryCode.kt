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

package me.proton.core.humanverification.domain.usecase

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import me.proton.core.humanverification.domain.exception.NoCountriesException
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import javax.inject.Inject

/**
 * Use case for the most used country calling code that should be presented as a suggested calling
 * code later in the UI.
 *
 * @author Dino Kadrikj.
 */
class MostUsedCountryCode
@Inject
constructor(private val humanVerificationLocalRepository: HumanVerificationLocalRepository) {

    /**
     * Returns the most used country calling code.
     */
    suspend operator fun invoke() = flow {
        humanVerificationLocalRepository.mostUsedCountries().collect {
            if (it.isNotEmpty()) {
                emit(it[0].callingCode)
            } else {
                throw NoCountriesException()
            }
        }
    }
}
