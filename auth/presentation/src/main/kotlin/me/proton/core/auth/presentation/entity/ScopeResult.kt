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

package me.proton.core.auth.presentation.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.network.domain.session.SessionId

@Parcelize
data class ScopeResult(
    val sessionId: String,
    val scopes: List<String>,
    val isTwoPassModeNeeded: Boolean = false
) : Parcelable {

    constructor(sessionId: SessionId, scopeInfo: ScopeInfo, isMailboxLoginNeeded: Boolean = false) :
        this(sessionId.id, scopeInfo.scopes, isMailboxLoginNeeded)
}
