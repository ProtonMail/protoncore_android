package ch.protonmail.libs.core.utils.internal

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService

/*
 * Internal utilities for Context
 * Author: Davide Farella
 */

/** @return [ConnectivityManager] from [Context] */
internal val Context.connectivityManager get() = getSystemService<ConnectivityManager>()
