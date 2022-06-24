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

package me.proton.core.crypto.validator.dagger

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.validator.data.prefs.CryptoPrefsImpl
import me.proton.core.crypto.validator.domain.prefs.CryptoPrefs
import me.proton.core.crypto.validator.presentation.CryptoValidator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CoreCryptoValidatorModule {

    @Provides
    @Singleton
    fun provideKeyStoreCryptoCheck(
        application: Application,
        keyStoreCrypto: KeyStoreCrypto,
        cryptoPrefs: CryptoPrefs,
    ): CryptoValidator =
        CryptoValidator(
            application,
            keyStoreCrypto,
            cryptoPrefs
        )
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CoreCryptoValidatorBindsModule {
    @Binds
    abstract fun bindCryptoPrefs(cryptoPrefsImpl: CryptoPrefsImpl): CryptoPrefs
}
