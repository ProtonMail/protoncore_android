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

package me.proton.core.test.android

import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.NetworkBindsModule
import me.proton.android.core.coreexample.di.NetworkConstantsModule
import me.proton.android.core.coreexample.di.WorkManagerModule
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpChallenge
import me.proton.core.crypto.dagger.CoreCryptoModule
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.paymentiap.dagger.CorePaymentIapBillingModule
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.test.android.mocks.FakeApiClient
import me.proton.core.test.android.mocks.FakeKeyStoreCrypto
import me.proton.core.test.android.mocks.FakePGPCrypto
import me.proton.core.test.android.mocks.FakeSrpCrypto
import me.proton.core.test.android.mocks.FakeSrpChallenge
import me.proton.core.test.android.mocks.FakeBillingClientFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        CoreCryptoModule::class,
        CorePaymentIapBillingModule::class,
        NetworkConstantsModule::class,
        NetworkBindsModule::class,
        WorkManagerModule::class
    ]
)
object TestComponent {
    // region CoreCryptoModule
    @Provides
    @Singleton
    fun provideKeyStoreCrypto(): KeyStoreCrypto = FakeKeyStoreCrypto()

    @Provides
    @Singleton
    fun provideCryptoContext(
        keyStoreCrypto: KeyStoreCrypto,
        srpCrypto: SrpCrypto
    ): CryptoContext = object : CryptoContext {
        override val keyStoreCrypto: KeyStoreCrypto = keyStoreCrypto
        override val pgpCrypto: PGPCrypto = FakePGPCrypto()
        override val srpCrypto: SrpCrypto = srpCrypto
    }

    @Provides
    @Singleton
    fun provideSrpCrypto(): SrpCrypto = FakeSrpCrypto()
    // endregion

    // region CorePaymentIapBillingModule
    @Provides
    @Singleton
    fun provideTestBillingClientFactory(): FakeBillingClientFactory = FakeBillingClientFactory()

    @Provides
    @Singleton
    fun provideBillingClientProvider(factory: FakeBillingClientFactory): BillingClientFactory = factory
    // endregion

    // region NetworkConstantsModule
    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = emptyArray()

    @CertificatePins
    @Provides
    fun provideCertificatePins() = emptyArray<String>()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins() = emptyList<String>()
    // endregion

    // region NetworkBindsModule

    @Provides
    @Singleton
    fun provideApiClient(): ApiClient = FakeApiClient()

    // endregion

    // region WorkManagerModule
    @Provides
    @Singleton
    fun provideWorkManager(): WorkManager = mockk(relaxed = true)
    // endregion

    @Provides
    @Singleton
    fun provideSrpChallenge(): SrpChallenge = FakeSrpChallenge()
}
