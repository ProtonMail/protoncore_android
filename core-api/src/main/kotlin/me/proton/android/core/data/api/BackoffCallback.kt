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

package me.proton.android.core.data.api

import android.os.Handler
import me.proton.android.core.data.api.exception.AuthorizeException
import retrofit2.Call
import retrofit2.Callback
import timber.log.Timber
import kotlin.math.max
import kotlin.math.pow

/**
 * Created by dinokadrikj on 4/1/20.
 *
 * Class that is dealing with API call retries with an exponential backoff strategy.
 */

private const val RETRY_COUNT = 2
private const val RETRY_DELAY = 200.0

abstract class BackoffCallback<T> : Callback<T> {
    private var retryCount = 0

    abstract fun onStart()

    override fun onFailure(call: Call<T>, t: Throwable) {
        if (!call.isCanceled) {
            if (retryCount == 0) {
                Timber.e(t)
            }
            retryCount++
            if (retryCount <= RETRY_COUNT && t !is AuthorizeException) {
                val expDelay =
                    (RETRY_DELAY * 2.0.pow(max(0, retryCount - 1).toDouble())).toInt()
                Handler().postDelayed({ retry(call) }, expDelay.toLong())
            } else {
                onFailedAfterRetry(t)
            }
        }
    }

    private fun retry(call: Call<T>) {
        call.clone().enqueue(this)
    }

    abstract fun onFailedAfterRetry(t: Throwable)
}