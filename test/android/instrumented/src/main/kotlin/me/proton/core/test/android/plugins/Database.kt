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

package me.proton.core.test.android.plugins

import android.util.Log
import me.proton.core.accountmanager.dagger.AccountManagerModule.provideAccountManagerDatabase
import me.proton.core.test.android.instrumented.ProtonTest.Companion.getContext
import me.proton.core.test.android.instrumented.ProtonTest.Companion.testTag

object Database {
    fun clearAccountManagerDb() {
        val db = provideAccountManagerDatabase(getContext())
        Log.d(testTag, "Clearing AccountManager database tables")
        db.clearAllTables()
    }
}
