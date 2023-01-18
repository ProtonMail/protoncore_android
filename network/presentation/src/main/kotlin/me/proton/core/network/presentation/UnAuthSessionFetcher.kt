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

package me.proton.core.network.presentation

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.proton.core.network.domain.session.unauth.OpportunisticUnAuthTokenRequest
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

public class UnAuthSessionFetcher @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val opportunisticUnAuthTokenRequest: OpportunisticUnAuthTokenRequest
) {
    private val scope get() = scopeProvider.GlobalIOSupervisedScope

    public fun fetch(): Job = scope.launch {
        opportunisticUnAuthTokenRequest()
    }
}