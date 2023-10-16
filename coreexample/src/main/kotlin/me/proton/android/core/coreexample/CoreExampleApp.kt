/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.android.core.coreexample

import android.app.Application
import androidx.startup.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import me.proton.android.core.coreexample.init.AccountStateHandlerInitializer
import me.proton.android.core.coreexample.init.EventManagerInitializer
import me.proton.android.core.coreexample.init.FeatureFlagInitializer
import me.proton.android.core.coreexample.init.LoggerInitializer
import me.proton.android.core.coreexample.init.WorkManagerInitializer
import me.proton.core.auth.presentation.MissingScopeInitializer
import me.proton.core.crypto.validator.presentation.init.CryptoValidatorInitializer
import me.proton.core.humanverification.presentation.HumanVerificationInitializer
import me.proton.core.keytransparency.presentation.init.KeyTransparencyInitializer
import me.proton.core.network.presentation.init.UnAuthSessionFetcherInitializer
import me.proton.core.plan.presentation.UnredeemedPurchaseInitializer
import me.proton.core.telemetry.presentation.ProductMetricsInitializer

@HiltAndroidApp
class CoreExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).apply {
            initializeComponent(WorkManagerInitializer::class.java)
            initializeComponent(EventManagerInitializer::class.java)
            initializeComponent(AccountStateHandlerInitializer::class.java)
            initializeComponent(CryptoValidatorInitializer::class.java)
            initializeComponent(UnredeemedPurchaseInitializer::class.java)
            initializeComponent(MissingScopeInitializer::class.java)
            initializeComponent(HumanVerificationInitializer::class.java)
            initializeComponent(UnAuthSessionFetcherInitializer::class.java)
            initializeComponent(KeyTransparencyInitializer::class.java)
            initializeComponent(LoggerInitializer::class.java)
            initializeComponent(FeatureFlagInitializer::class.java)
            initializeComponent(ProductMetricsInitializer::class.java)
        }
    }
}
