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

package me.proton.core.crypto.validator.presentation

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.validator.domain.prefs.CryptoPrefs
import me.proton.core.crypto.validator.presentation.ui.CryptoValidatorErrorDialogActivity
import me.proton.core.presentation.utils.EmptyActivityLifecycleCallbacks
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

public class CryptoValidator @Inject constructor(
    private val application: Application,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val cryptoPrefs: CryptoPrefs,
    private val scopeProvider: CoroutineScopeProvider
) {
    private val scope get() = scopeProvider.GlobalIOSupervisedScope

    public fun validate(): Job = scope.launch {
        if (cryptoPrefs.useInsecureKeystore == true || keyStoreCrypto.isUsingKeyStore()) return@launch

        application.registerActivityLifecycleCallbacks(object : EmptyActivityLifecycleCallbacks() {
            /** Try to display error activity whenever an activity is resumed. */
            override fun onActivityResumed(activity: Activity) {
                if (activity !is CryptoValidatorErrorDialogActivity) {
                    CryptoValidatorErrorDialogActivity.show(activity)
                }
            }

            /** Don't check again if user accepted the risk of not using proper encryption. */
            override fun onActivityDestroyed(activity: Activity) {
                if (activity !is CryptoValidatorErrorDialogActivity) return

                val callbacks = this
                scope.launch {
                    if (cryptoPrefs.useInsecureKeystore == true) {
                        application.unregisterActivityLifecycleCallbacks(callbacks)
                    }
                }
            }
        })
    }
}
