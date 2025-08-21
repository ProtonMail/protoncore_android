package me.proton.android.core.coreexample

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import me.proton.android.core.coreexample.init.AccountStateHandlerInitializer
import me.proton.android.core.coreexample.init.EventManagerInitializer
import me.proton.android.core.coreexample.init.FeatureFlagInitializer
import me.proton.android.core.coreexample.init.LoggerInitializer
import me.proton.android.core.coreexample.init.StrictModeInitializer
import me.proton.android.core.coreexample.init.WorkManagerInitializer
import me.proton.core.auth.presentation.MissingScopeInitializer
import me.proton.core.crypto.validator.presentation.init.CryptoValidatorInitializer
import me.proton.core.humanverification.presentation.HumanVerificationInitializer
import me.proton.core.keytransparency.presentation.init.KeyTransparencyInitializer
import me.proton.core.network.presentation.init.UnAuthSessionFetcherInitializer
import me.proton.core.paymentiap.presentation.GooglePurchaseHandlerInitializer
import me.proton.core.plan.presentation.PurchaseHandlerInitializer
import me.proton.core.plan.presentation.UnredeemedPurchaseInitializer
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryInitializer

class MainInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // No-op needed
    }

    override fun dependencies() = listOf(
        EventManagerInitializer::class.java,
        DeviceRecoveryInitializer::class.java,
        CryptoValidatorInitializer::class.java,
        PurchaseHandlerInitializer::class.java,
        GooglePurchaseHandlerInitializer::class.java,
        UnredeemedPurchaseInitializer::class.java,
        MissingScopeInitializer::class.java,
        HumanVerificationInitializer::class.java,
        UnAuthSessionFetcherInitializer::class.java,
        KeyTransparencyInitializer::class.java,
        LoggerInitializer::class.java,
        FeatureFlagInitializer::class.java,
        AccountStateHandlerInitializer::class.java
    )

    companion object {
        fun init(appContext: Context) {
            with(AppInitializer.getInstance(appContext)) {
                initializeComponent(MainInitializer::class.java)
            }
        }
    }
}
