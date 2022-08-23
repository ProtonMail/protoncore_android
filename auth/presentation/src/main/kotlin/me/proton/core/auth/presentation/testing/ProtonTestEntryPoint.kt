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

package me.proton.core.auth.presentation.testing

import androidx.annotation.RestrictTo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders

@EntryPoint
@InstallIn(SingletonComponent::class)
@RestrictTo(RestrictTo.Scope.TESTS)
interface ProtonTestEntryPoint {
    val getAvailablePaymentProviders: GetAvailablePaymentProviders
    val loginTestHelper: LoginTestHelper
}
