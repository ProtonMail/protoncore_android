package me.proton.core.configuration.configurator

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.TreeMap


class ConfigContentProvider : ContentProvider() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = ENVIRONMENT_CONFIG_PREFERENCES)

    private lateinit var appContext: Context

    override fun onCreate(): Boolean {
        context?.let {
            appContext = it
            return true
        }
        return false
    }

    override fun getType(uri: Uri): String = UriType.Item.value

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor = runBlocking {
        appContext.dataStore.data.first().asMap().mapKeys {
            it.key.name
        }.toMatrixCursor()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri = runBlocking {
        appContext.dataStore.edit { preferences ->
            values?.keySet()?.forEach { key ->
                preferences[stringPreferencesKey(key)] = values.getAsString(key)
            } ?: error("Values cannot be null for Insert operation!")
        }
        return@runBlocking uri
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("delete() is not supported")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("update() is not supported")
    }

    private fun Map<String, Any?>.toMatrixCursor(): Cursor {
        val keys = keys.sorted().toTypedArray()
        val values = TreeMap(this).values.toTypedArray()
        return MatrixCursor(keys).apply { addRow(values) }
    }

    private enum class UriType(val value: String) {
        Item("vnd.android.cursor.item/vnd.proton.core.test.config")
    }

    companion object {
        const val ENVIRONMENT_CONFIG_PREFERENCES = "environmentConfigPreferences"
    }
}
