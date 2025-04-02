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

package me.proton.core.auth.domain.usecase

import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.network.domain.session.Session
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

class CreateLoginSessionFromFork @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(
        accountType: AccountType,
        passphrase: EncryptedByteArray?,
        session: Session.Authenticated
    ) {
        val account = Account(
            userId = session.userId,
            username = null,
            email = null,
            state = AccountState.NotReady,
            sessionId = session.sessionId,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(
                session = SessionDetails(
                    initialEventId = null,
                    requiredAccountType = accountType,
                    secondFactorEnabled = false,
                    twoPassModeEnabled = false,
                    passphrase = passphrase,
                    password = null,
                    fido2AuthenticationOptionsJson = null,
                )
            )
        )
        accountWorkflowHandler.handleSession(account, session)

        val user = userManager.getUser(session.userId)
        accountRepository.createOrUpdateAccountSession(
            account = account.copy(username = user.name, email = user.email),
            session = session
        )
    }
}
