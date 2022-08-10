/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.paymentcommon.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.paymentcommon.data.ProtonIAPBillingLibraryImpl
import me.proton.core.paymentcommon.domain.usecase.ProtonIAPBillingLibrary
import me.proton.core.paymentcommon.presentation.ActivePaymentProvider
import me.proton.core.paymentcommon.presentation.viewmodel.ActivePaymentProviderImpl

@Module
@InstallIn(SingletonComponent::class)
public interface CorePaymentModule {

    @Binds
    public fun provideGooglePlayBillingLibrary(impl: ProtonIAPBillingLibraryImpl): ProtonIAPBillingLibrary

    @Binds
    public fun provideActivePaymentProvider(impl: ActivePaymentProviderImpl): ActivePaymentProvider
}
