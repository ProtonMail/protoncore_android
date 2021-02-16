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

package me.proton.core.user.data

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

object TestAccounts {
    object User1 {
        val account = Account(
            userId = TestUsers.User1.id,
            username = TestUsers.User1.id.id,
            email = TestUsers.User1.response.email,
            state = AccountState.Ready,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated
        )
    }

    object User2 {
        val account = Account(
            userId = TestUsers.User2.id,
            username = TestUsers.User2.id.id,
            email = TestUsers.User2.response.email,
            state = AccountState.Ready,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated
        )
    }

    val sessionId = SessionId("sessionId")
    val session = Session(
        sessionId = sessionId,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        headers = null,
        scopes = emptyList()
    )
}
