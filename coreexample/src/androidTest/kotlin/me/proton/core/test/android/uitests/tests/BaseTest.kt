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

package me.proton.core.test.android.uitests.tests

import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.di.AppDatabaseModule
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.android.instrumented.utils.Shell.setupDevice
import me.proton.core.test.android.plugins.data.User.Users
import org.junit.After
import org.junit.BeforeClass

open class BaseTest : ProtonTest(MainActivity::class.java) {

    @After
    override fun tearDown() {
        super.tearDown()
        db.clearAllTables()
    }

    companion object {
        val users = Users("sensitive/users.json")
        val db = AppDatabaseModule.provideAppDatabase(getContext())

        @JvmStatic
        @BeforeClass
        fun prepare() {
            setupDevice(true)
            db.clearAllTables()
        }
    }
}
