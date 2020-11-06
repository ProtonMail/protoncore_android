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

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.setCompatVectorFromResourcesEnabled
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class CoreExampleApp : Application() {

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, e: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
            /*when (priority) {
                Log.VERBOSE,
                Log.DEBUG -> Unit
                Log.ERROR -> CrashLibrary.logError()
                Log.WARN -> CrashLibrary.logWarning()
                else -> CrashLibrary.log()
            }*/
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        setCompatVectorFromResourcesEnabled(true)
    }
}
