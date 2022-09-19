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

package me.proton.core.presentation.app

import android.app.Activity
import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.presentation.utils.EmptyActivityLifecycleCallbacks
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider @Inject constructor(
    @ApplicationContext
    private val context: Context
) : EmptyActivityLifecycleCallbacks() {

    private var weakRef: WeakReference<Activity>? = null

    val lastResumed: Activity? get() = weakRef?.get()

    init {
        val app = context as Application
        app.registerActivityLifecycleCallbacks(this@ActivityProvider)
    }

    override fun onActivityResumed(activity: Activity) {
        weakRef = WeakReference(activity)
    }
}
