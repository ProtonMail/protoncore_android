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

package me.proton.android.core.coreexample

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.presentation.srp.CryptoProviderImpl
import me.proton.core.auth.presentation.srp.SrpProofProviderImpl
import me.proton.core.network.domain.ApiClient

/**
 * @author Dino Kadrikj.
 */
@Module
@InstallIn(ApplicationComponent::class)
abstract class ApplicationBindsModule {

    @Binds
    abstract fun provideSrpProofProvider(srpProofProviderImpl: SrpProofProviderImpl): SrpProofProvider

    @Binds
    abstract fun provideApiClient(coreExampleApiClient: CoreExampleApiClient): ApiClient

    @Binds
    abstract fun provideCryptoProvider(cryptoProviderImpl: CryptoProviderImpl): CryptoProvider
}
