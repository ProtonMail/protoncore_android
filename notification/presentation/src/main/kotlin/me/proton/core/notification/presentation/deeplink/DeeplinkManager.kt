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

package me.proton.core.notification.presentation.deeplink

import android.content.Context
import android.content.Intent
import me.proton.core.util.kotlin.takeIfNotEmpty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DeeplinkManager @Inject constructor() {

    private data class Path(val path: String, val callback: (DeeplinkContext) -> Boolean)

    private val paths = mutableListOf<Path>()

    /**
     * Register a [path] and an [action] to perform when [handle] is called with a matching [Intent].
     *
     * Example of path:
     * ```
     * - "app/setting"
     * - "app/setting/theme"
     * - "user/{userId}/notification/{notificationId}/open/{type}"
     * - "user/{userId}/notification/{notificationId}/delete"
     * - "user/{userId}/password/change"
     * - "message/{messageId}/open"
     * - "message/{messageId}/makeAsRead"
     * ```
     * Example of usage:
     * ```
     * class MyApplication : Application() {
     *
     *     override fun onCreate() {
     *         super.onCreate()
     *         ...
     *         deeplinkManager.register("my/object/{id}/view") { link ->
     *             val id = link.args[0]
     *             link.context?.startActivityIfNeeded(...) ?: false
     *         }
     *         deeplinkManager.register("my/object/{id}/delete") { link ->
     *             val id = link.args[0]
     *             delete(id)
     *         }
     *     }
     * }
     *
     * class MyActivity : Activity() {
     *
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
     *         addOnNewIntentListener { deeplinkManager.handle(intent = it, context = context) }
     *         ...
     *     }
     *     ...
     *     private fun createNotification(id: String) {
     *         ...
     *         val openIntent = deeplinkIntentProvider.getActivityPendingIntent("my/object/$id/view")
     *         val deleteIntent = deeplinkIntentProvider.getBroadcastPendingIntent("my/object/$id/delete")
     *
     *         val builder = NotificationCompat.Builder(this, CHANNEL_ID)
     *         builder.setContentIntent(openIntent)
     *         builder.setDeleteIntent(deleteIntent)
     *         ...
     *     }
     * }
     * ```
     *
     * Note: You should never perform long-running operations in [action]. If you need to perform any follow up
     * background work, schedule a [Worker] using [WorkManager].
     *
     * @see [DeeplinkIntentProvider]
     */
    public fun register(path: String, action: (DeeplinkContext) -> Boolean) {
        // Replace argument pattern by regex group pattern, and Escape "/".
        val regex = path.replace(argumentRegex, "(.*)").replace("/", "\\/")
        paths.add(Path(regex, action))
    }

    /**
     * Invoke an existing action registered via [register] when the [intent] match the path.
     *
     * @param intent an intent to try to match with existing [register] action.
     * @param context an optional context (e.g. Activity) to set in [DeeplinkContext.context].
     * @return true if [intent] has been handled, false otherwise.
     */
    public fun handle(intent: Intent?, context: Context? = null): Boolean {
        val path = intent?.data?.path?.removePrefix("/") ?: return false
        return paths.filter { current ->
            current.path.toRegex().matches(path)
        }.fold(initial = 0) { acc, current ->
            val results = current.path.toRegex().matchEntire(path)
            val args = results?.groupValues.orEmpty()
                .drop(1) // First is the path.
                .mapNotNull { it.substringBefore("/").takeIfNotEmpty() } // Remove extra path.
            acc + (1.takeIf { current.callback.invoke(DeeplinkContext(context, path, args)) } ?: 0)
        } >= 1
    }

    internal companion object {
        private val argumentRegex = "\\{.*?\\}".toRegex()
        private const val scheme = "app"
        private const val domain = "me.proton"
        const val uriPrefix = "$scheme://$domain"
    }
}

/**
 * Context for Deeplink callback.
 *
 * @see [DeeplinkManager.register]
 */
public data class DeeplinkContext(
    /**
     * Optional context (e.g. Activity, Application, ...) set by [DeeplinkManager.handle].
     */
    val context: Context?,
    /**
     * Path of Deeplink.
     *
     * Example:
     * ```
     * - "app/setting"
     * - "app/setting/theme"
     * - "user/{userId}/notification/{notificationId}/open/{type}"
     * - "user/{userId}/notification/{notificationId}/delete"
     * - "user/{userId}/password/change"
     * - "message/{messageId}/open"
     * - "message/{messageId}/makeAsRead"
     * ```
     */
    val path: String,
    /**
     * Parsed argument(s) from [path] containing all dynamic part of the path.
     *
     * Example:
     * ```
     * - If path = "user/12345/action" -> args = listOf("12345")
     * - If path = "user/12345/action/54321" -> args = listOf("12345", "54321")
     * - If path = "app/setting/theme" -> args = emptyList()
     */
    val args: List<String>
)
