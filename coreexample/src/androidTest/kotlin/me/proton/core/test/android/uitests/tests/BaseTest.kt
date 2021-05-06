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

import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.android.core.coreexample.BuildConfig
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.accountmanager.dagger.AccountManagerModule
import me.proton.core.test.android.instrumented.CoreTest
import me.proton.core.test.android.instrumented.watchers.TestExecutionWatcher
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.RuleChain
import java.net.HttpURLConnection
import java.net.URL

open class BaseTest : CoreTest() {

    private val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private val testExecutionWatcher = TestExecutionWatcher()

    @Rule
    @JvmField
    val ruleChain = RuleChain
        .outerRule(testName)
        .around(testExecutionWatcher)
        .around(activityRule)!!

    @After
    override fun tearDown() {
        super.tearDown()
        clearAccountManagerDb()
    }

    fun jailUnban() {
        val url = URL("https://${BuildConfig.ENVIRONMENT}${BuildConfig.JAIL_UNBAN_ENDPOINT}")
        with(url.openConnection() as HttpURLConnection) { requestMethod = "GET" }
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun clearAccountManagerDb() {
            val db = AccountManagerModule.provideAccountManagerDatabase(
                InstrumentationRegistry.getInstrumentation().targetContext
            )
            Log.d(testTag, "Clearing AccountManager database tables")
            db.clearAllTables()
        }
    }
}
