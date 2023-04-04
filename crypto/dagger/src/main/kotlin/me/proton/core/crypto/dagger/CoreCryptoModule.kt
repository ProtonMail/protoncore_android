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

package me.proton.core.crypto.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.keystore.AndroidKeyStoreCrypto
import me.proton.core.crypto.android.srp.GOpenPGPSrpChallenge
import me.proton.core.crypto.android.srp.GOpenPGPSrpCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.srp.SrpChallenge
import me.proton.core.crypto.common.srp.SrpCrypto
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object CoreCryptoModule {

    @Provides
    @Singleton
    public fun provideKeyStoreCrypto(): KeyStoreCrypto =
        AndroidKeyStoreCrypto.default

    @Provides
    @Singleton
    public fun provideCryptoContext(
        keyStoreCrypto: KeyStoreCrypto
    ): CryptoContext =
        AndroidCryptoContext(keyStoreCrypto)

    @Provides
    @Singleton
    public fun provideSrpCrypto(): SrpCrypto = GOpenPGPSrpCrypto()

    @Provides
    @Singleton
    public fun provideSrpChallenge(): SrpChallenge = GOpenPGPSrpChallenge()
}
