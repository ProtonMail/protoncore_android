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

package me.proton.core.report.dagger

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

internal class AppUtils @Inject constructor(@ApplicationContext private val appContext: Context) {
    fun appVersionName(): String = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: ""

    /** Returns app name.
     * The name is read from AndroidManifest's `<application android:label>`.
     * If the label is defined via a string resource, we always try to
     * load the untranslated (default) name.
     */
    fun appName(): String {
        val defaultConfig = Configuration(appContext.resources.configuration).apply { setLocale(Locale.ROOT) }
        val context = appContext.createConfigurationContext(defaultConfig)
        val labelRes = context.applicationInfo.labelRes
        return if (labelRes != 0) {
            context.resources.getString(labelRes)
        } else {
            context.packageManager.getApplicationLabel(context.applicationInfo).toString()
        }
    }
}
