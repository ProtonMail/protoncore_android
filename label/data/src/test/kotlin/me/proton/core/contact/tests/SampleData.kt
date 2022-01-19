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
import me.proton.core.domain.entity.UserId
import me.proton.core.label.data.local.LabelEntity
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.user.data.entity.UserEntity

object User0 {
    val userId = UserId("user0")
    val userEntity = userId.userEntity()
    val accountEntity = userId.accountEntity()

    val label0Id = LabelId("label0")
    val label0Type = LabelType.MessageLabel
    val label0Entity = userId.labelEntity(label0Id, label0Type)
}

fun UserId.userEntity() = UserEntity(
    userId = this,
    email = null,
    name = null,
    displayName = null,
    currency = "EUR",
    credit = 0,
    usedSpace = 0,
    maxSpace = 0,
    maxUpload = 0,
    role = null,
    isPrivate = false,
    subscribed = 0,
    services = 0,
    delinquent = null,
    passphrase = null
)

fun UserId.accountEntity() = AccountEntity(
    userId = this,
    username = "",
    email = null,
    state = AccountState.Ready,
    sessionId = null,
    sessionState = null
)

fun UserId.labelEntity(labelId: LabelId, type: LabelType) = LabelEntity(
    userId = this,
    labelId = labelId.id,
    parentId = null,
    name = labelId.id,
    type = type.value,
    path = labelId.id,
    color = "#RRGGBB",
    order = 0,
    isNotified = null,
    isExpanded = null,
    isSticky = null
)
