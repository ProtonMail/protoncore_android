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

package me.proton.core.mailsettings.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

@Entity(
    primaryKeys = ["userId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MailSettingsEntity(
    val userId: UserId,
    val displayName: String?,
    val signature: String?,
    val autoSaveContacts: Int?,
    val composerMode: Int?,
    val messageButtons: Int?,
    val showImages: Int?,
    val showMoved: Int?,
    val viewMode: Int?,
    val viewLayout: Int?,
    val swipeLeft: Int?,
    val swipeRight: Int?,
    val shortcuts: Int?,
    val pmSignature: Int?,
    val numMessagePerPage: Int?,
    val draftMimeType: String?,
    val receiveMimeType: String?,
    val showMimeType: String?,
    val enableFolderColor: Int?,
    val inheritParentFolderColor: Int?,
    val rightToLeft: Int?,
    val attachPublicKey: Int?,
    val sign: Int?,
    val pgpScheme: Int?,
    val promptPin: Int?,
    val stickyLabels: Int?,
    val confirmLink: Int?
)
