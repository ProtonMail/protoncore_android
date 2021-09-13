/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.contact.tests

import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.contact.data.local.db.ContactEntity
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

val user0_id = UserId("0")
val user0 = UserEntity(
    userId = user0_id,
    email = null,
    name = null,
    displayName = null,
    currency = "EUR",
    credit = 0,
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    private = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    passphrase = null
)
val account0 = AccountEntity(
    userId = user0_id,
    username = "",
    email = null,
    state = AccountState.Ready,
    sessionId = null,
    sessionState = null
)

val contact1_id = ContactId("1")
val contact1 = ContactEntity(
    userId = user0_id,
    contactId = contact1_id,
    name = "contact1"
)
