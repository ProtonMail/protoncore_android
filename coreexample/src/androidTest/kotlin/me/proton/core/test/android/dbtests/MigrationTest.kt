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

package me.proton.core.test.android.dbtests

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import me.proton.android.core.coreexample.db.AppDatabase
import me.proton.core.network.domain.humanverification.HumanVerificationState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertContentEquals

class MigrationTest {
    private val testDb = "migration-test"

    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(testDb, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDb
        ).addMigrations(*AppDatabase.migrations.toTypedArray()).build().apply {
            openHelper.writableDatabase
            close()
        }
    }

    @Test
    fun migrateHVEntities_22_23() = runTest {
        val db = helper.createDatabase(testDb, 22)
        db.insertHVEntity_v22("c-1", HumanVerificationState.HumanVerificationNeeded)
        db.insertHVEntity_v22("c-2", HumanVerificationState.HumanVerificationFailed)
        db.insertHVEntity_v22("c-3", HumanVerificationState.HumanVerificationSuccess)
        db.close()

        val appDb = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDb
        ).addMigrations(*AppDatabase.migrations.toTypedArray()).build()

        val clientIds = mutableListOf<String>()
        appDb.openHelper.writableDatabase.query("SELECT clientId FROM HumanVerificationEntity").use {
            while (it.moveToNext()) {
                clientIds.add(it.getString(it.getColumnIndex("clientId")))
            }
        }
        appDb.close()

        assertContentEquals(listOf("c-3"), clientIds)
    }

    private fun SupportSQLiteDatabase.insertHVEntity_v22(clientId: String, state: HumanVerificationState) {
        val values = ContentValues().apply {
            put("clientId", clientId)
            put("clientIdType", "session")
            put("verificationMethods", "captcha")
            put("state", state.name)
        }
        insert("HumanVerificationEntity", SQLiteDatabase.CONFLICT_FAIL, values)
    }
}
