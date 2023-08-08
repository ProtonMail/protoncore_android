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

package me.proton.android.core.coreexample.init

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import timber.log.Timber

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
        CoreLogger.set(TimberLogger)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        SentryInitializer::class.java
    )

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
            /*when (priority) {
                Log.VERBOSE,
                Log.DEBUG -> Unit0
                Log.ERROR -> CrashLibrary.logError()
                Log.WARN -> CrashLibrary.logWarning()
                else -> CrashLibrary.log()
            }*/
        }
    }
}
