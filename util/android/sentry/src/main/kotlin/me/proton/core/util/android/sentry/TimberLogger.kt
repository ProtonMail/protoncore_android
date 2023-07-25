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

package me.proton.core.util.android.sentry

import me.proton.core.accountmanager.domain.LogTag.SESSION_FORCE_LOGOUT
import me.proton.core.eventmanager.domain.LogTag.REPORT_MAX_RETRY
import me.proton.core.network.domain.LogTag.API_ERROR
import me.proton.core.network.domain.LogTag.API_REQUEST
import me.proton.core.network.domain.LogTag.API_RESPONSE
import me.proton.core.network.domain.LogTag.SERVER_TIME_PARSE_ERROR
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.LoggerLogTag
import org.jetbrains.annotations.NonNls
import timber.log.Timber

public object TimberLogger : Logger {

    override fun e(tag: String, e: Throwable) {
        logError(tag, e)
    }

    override fun e(tag: String, e: Throwable, @NonNls message: String) {
        logError(tag, e, message)
    }

    private fun logError(tag: String, e: Throwable, message: String? = null) {
        when (e) {
            is ApiException -> when (e.error) {
                is ApiResult.Error.Connection -> Timber.tag(tag).w(e, message)
                else -> Timber.tag(tag).e(e, message)
            }

            else -> Timber.tag(tag).e(e, message)
        }
    }

    override fun i(tag: String, @NonNls message: String) {
        Timber.tag(tag).i(message)
    }

    override fun i(tag: String, e: Throwable, message: String) {
        Timber.tag(tag).i(e, message)
    }

    override fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    override fun d(tag: String, e: Throwable, message: String) {
        Timber.tag(tag).d(e, message)
    }

    override fun v(tag: String, message: String) {
        Timber.tag(tag).v(message)
    }

    override fun v(tag: String, e: Throwable, message: String) {
        Timber.tag(tag).v(e, message)
    }

    override fun log(tag: LoggerLogTag, message: String) {
        when (tag) {
            API_REQUEST, API_RESPONSE -> Timber.tag(tag.name).i(message)
            SERVER_TIME_PARSE_ERROR, REPORT_MAX_RETRY, SESSION_FORCE_LOGOUT -> Timber.tag(tag.name).e(message)
            API_ERROR -> Timber.tag(tag.name).w(message)
            else -> Timber.tag(tag.name).d(message)
        }
    }
}