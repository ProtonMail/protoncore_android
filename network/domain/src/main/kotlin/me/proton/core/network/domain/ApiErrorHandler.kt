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
package me.proton.core.network.domain

/**
 * Handler for API call errors. To be used as a part of a chain of handlers.
 *
 * @param Api API interface
 */
interface ApiErrorHandler<Api> {

    /**
     * Handles the API call error and returns new result or leaves error unhandled.
     *
     * @param T Api call result type
     * @param backend [ApiBackend] that handler should use to execute calls
     * @param error Error to be handled
     * @param call [ApiManager.Call] to be made.
     * @return new [ApiResult] or original [error] if not handled
     */
    suspend operator fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T>
}
