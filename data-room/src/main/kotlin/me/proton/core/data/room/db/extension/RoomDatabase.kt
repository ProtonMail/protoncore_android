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

package me.proton.core.data.room.db.extension

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Create and/or open a database that will be used for reading and writing.
 *
 * Any needed/defined migrations will be applied.
 *
 * Note: Make sure to call close when you no longer need the database
 */
fun RoomDatabase.open(): SupportSQLiteDatabase = openHelper.writableDatabase

/**
 * Create and/or open a database and directly close it after creation/upgrade/migration.
 *
 * Any needed/defined migrations will be applied.
 */
fun RoomDatabase.openAndClose(): Unit = openHelper.writableDatabase.close()
