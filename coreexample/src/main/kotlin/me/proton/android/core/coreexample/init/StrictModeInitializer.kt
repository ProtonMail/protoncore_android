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

package me.proton.android.core.coreexample.init

import android.content.Context
import android.os.StrictMode
import androidx.startup.Initializer
import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.util.android.strictmode.detectCommon

class StrictModeInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            applyStrictMode()
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    private fun applyStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyFlashScreen()
                .penaltyLog()
                .penaltyDeathOnNetwork()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectCommon()
                .penaltyLog()
                .build()
        )
    }
}
