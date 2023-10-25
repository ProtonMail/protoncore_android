package me.proton.core.notification.presentation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

public class NotificationDataStoreProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.permissionDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "permissionDataStore"
    )

    public val permissionDataStore: DataStore<Preferences> = context.permissionDataStore
}
