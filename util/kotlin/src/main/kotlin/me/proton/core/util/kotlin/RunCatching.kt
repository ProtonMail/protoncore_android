/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.util.kotlin

/**
 * Runs the given block of code and returns the result.
 * Any [RuntimeException] or [Error] are re-thrown
 * (e.g. [kotlin.coroutines.cancellation.CancellationException] or [OutOfMemoryError]).
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R> runCatchingCheckedExceptions(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: RuntimeException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
