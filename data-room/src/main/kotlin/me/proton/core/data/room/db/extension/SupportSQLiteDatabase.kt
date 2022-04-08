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

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Get [table] columns name.
 */
fun SupportSQLiteDatabase.getTableColumns(table: String): List<String> {
    return query("SELECT * FROM $table LIMIT 0").use { it.columnNames.toList() }
}

/**
 * Recreate [table] with [columns] using [createTable] and [createIndices], and then copy the old table data to it.
 */
fun SupportSQLiteDatabase.recreateTable(
    table: String,
    createTable: SupportSQLiteDatabase.() -> Unit,
    createIndices: SupportSQLiteDatabase.() -> Unit,
    columns: List<String>
) {
    val columnsSeparated = columns.joinToString(",")
    execSQL("ALTER TABLE $table RENAME TO ${table}_old")
    createTable.invoke(this)
    execSQL("INSERT INTO $table($columnsSeparated) SELECT $columnsSeparated FROM ${table}_old")
    execSQL("DROP TABLE ${table}_old")
    createIndices.invoke(this)
}

/**
 * Recreate [table] using [createTable] and [createIndices], and then copy the old table data to it.
 */
fun SupportSQLiteDatabase.recreateTable(
    table: String,
    createTable: SupportSQLiteDatabase.() -> Unit,
    createIndices: SupportSQLiteDatabase.() -> Unit
) = recreateTable(table, createTable, createIndices, getTableColumns(table))

/**
 * Add [column] to [table] with [type] and [defaultValue].
 */
fun SupportSQLiteDatabase.addTableColumn(
    table: String,
    column: String,
    type: String,
    defaultValue: String? = null,
    ifNotExists: Boolean = true
) {
    if (ifNotExists && columnExists(table = table, column = column)) return
    val defaultValueSuffix = defaultValue?.let { "DEFAULT '$defaultValue'" } ?: ""
    val sqlStatement = "ALTER TABLE $table ADD COLUMN $column $type $defaultValueSuffix"
    execSQL(sqlStatement)
}

/** Returns `true` if a [column] already exists in a [table]. */
fun SupportSQLiteDatabase.columnExists(table: String, column: String): Boolean {
    return column in getTableColumns(table)
}

/**
 * Drop [columns] from [table].
 *
 * Create a new table without the [columns], and then copy the old table data to it.
 *
 * Note: [createTable] and [createIndices] must be provided.
 */
fun SupportSQLiteDatabase.dropTableColumn(
    table: String,
    createTable: SupportSQLiteDatabase.() -> Unit,
    createIndices: SupportSQLiteDatabase.() -> Unit,
    columns: List<String>
) = recreateTable(table, createTable, createIndices, getTableColumns(table) - columns)

/**
 * Drop [column] from [table].
 *
 * Create a new table without the [column], and then copy the old table data to it.
 *
 * Note: [createTable] and [createIndices] must be provided.
 */
fun SupportSQLiteDatabase.dropTableColumn(
    table: String,
    createTable: SupportSQLiteDatabase.() -> Unit,
    createIndices: SupportSQLiteDatabase.() -> Unit,
    column: String
) = dropTableColumn(table, createTable, createIndices, listOf(column))

/**
 * Drop content from [table].
 */
fun SupportSQLiteDatabase.dropTableContent(table: String) {
    execSQL("DELETE FROM $table")
}

/**
 * Drop [table], if exist.
 */
fun SupportSQLiteDatabase.dropTable(table: String) {
    execSQL("DROP TABLE IF EXISTS $table")
}
