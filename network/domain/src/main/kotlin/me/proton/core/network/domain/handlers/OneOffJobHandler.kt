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

package me.proton.core.network.domain.handlers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Generic one-off job handler. Use this class in a situations when there is a need to execute a single
 * blocking one-off job. It will make sure only single job is running until completed.
 *
 * @author Dino Kadrikj.
 */
open class OneOffJobHandler<IN, OUT>(
    private val singleThreadScope: CoroutineScope
) {
    private var job: Deferred<OUT>? = null

    /**
     * Starts one-off job. Will prevent multiple time calls to start more than one parallel job.
     * So in other words makes sure not to start additional instances if the job is running.
     *
     * @param inputParameter the parameter needed for the Job.
     * @param block higher-order function representing the Job that should be executed.
     */
    suspend fun startOneOffJob(inputParameter: IN, block: suspend (IN) -> OUT): OUT =
        withContext(singleThreadScope.coroutineContext) {
            job = job ?: async {
                val result = block(inputParameter)
                job = null
                result
            }
            job!!.await()
        }
}
