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

package me.proton.core.network.data.di

import javax.inject.Qualifier

/** Qualifier for the base URL for the Proton API.
 * E.g. `https://api.proton-host.com`.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseProtonApiUrl

/** Qualifier for a shared (singleton) instance of [okhttp3.OkHttpClient].
 * Whenever you need a custom [okhttp3.OkHttpClient], inject this instance,
 * and use [okhttp3.OkHttpClient.newBuilder] to adjust to your own needs.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SharedOkHttpClient
