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

package me.proton.core.mailsettings.data.extension

import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.data.api.response.MailSettingsResponse
import me.proton.core.mailsettings.data.api.response.MobileSettingsResponse
import me.proton.core.mailsettings.data.api.response.ToolbarSettingsResponse
import me.proton.core.mailsettings.data.entity.ActionsToolbarEntity
import me.proton.core.mailsettings.data.entity.MailMobileSettingsEntity
import me.proton.core.mailsettings.data.entity.MailSettingsEntity
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.MobileSettings
import me.proton.core.mailsettings.domain.entity.PMSignature
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.mailsettings.domain.entity.ShowImage
import me.proton.core.mailsettings.domain.entity.ShowMoved
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ToolbarAction
import me.proton.core.mailsettings.domain.entity.ActionsToolbarSetting
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.util.kotlin.toBoolean
import me.proton.core.util.kotlin.toInt

fun MailSettingsResponse.toMailSettings(userId: UserId): MailSettings = fromResponse(userId = userId)

internal fun MailSettings.toEntity() = MailSettingsEntity(
    userId = userId,
    displayName = displayName,
    signature = signature,
    autoSaveContacts = autoSaveContacts?.toInt(),
    composerMode = composerMode?.value,
    messageButtons = messageButtons?.value,
    showImages = showImages?.value,
    showMoved = showMoved?.value,
    viewMode = viewMode?.value,
    viewLayout = viewLayout?.value,
    swipeLeft = swipeLeft?.value,
    swipeRight = swipeRight?.value,
    shortcuts = shortcuts?.toInt(),
    pmSignature = pmSignature?.value,
    numMessagePerPage = numMessagePerPage,
    autoDeleteSpamAndTrashDays = autoDeleteSpamAndTrashDays,
    draftMimeType = draftMimeType?.value,
    receiveMimeType = receiveMimeType?.value,
    showMimeType = showMimeType?.value,
    enableFolderColor = enableFolderColor?.toInt(),
    inheritParentFolderColor = inheritParentFolderColor?.toInt(),
    rightToLeft = rightToLeft?.toInt(),
    attachPublicKey = attachPublicKey?.toInt(),
    sign = sign?.toInt(),
    pgpScheme = pgpScheme?.value,
    promptPin = promptPin?.toInt(),
    stickyLabels = stickyLabels?.toInt(),
    confirmLink = confirmLink?.toInt(),
    mobileSettingsEntity = mobileSettings?.toEntity()
)

internal fun MailSettingsResponse.fromResponse(userId: UserId) = MailSettings(
    userId = userId,
    displayName = displayName,
    signature = signature,
    autoSaveContacts = autoSaveContacts?.toBoolean(),
    composerMode = ComposerMode.enumOf(composerMode),
    messageButtons = MessageButtons.enumOf(messageButtons),
    showImages = ShowImage.enumOf(showImages),
    showMoved = ShowMoved.enumOf(showMoved),
    viewMode = ViewMode.enumOf(viewMode),
    viewLayout = ViewLayout.enumOf(viewLayout),
    swipeLeft = SwipeAction.enumOf(swipeLeft),
    swipeRight = SwipeAction.enumOf(swipeRight),
    shortcuts = shortcuts?.toBoolean(),
    pmSignature = PMSignature.enumOf(pmSignature),
    numMessagePerPage = numMessagePerPage,
    autoDeleteSpamAndTrashDays = autoDeleteSpamAndTrashDays,
    draftMimeType = MimeType.enumOf(draftMimeType),
    receiveMimeType = MimeType.enumOf(receiveMimeType),
    showMimeType = MimeType.enumOf(showMimeType),
    enableFolderColor = enableFolderColor?.toBoolean(),
    inheritParentFolderColor = inheritParentFolderColor?.toBoolean(),
    rightToLeft = rightToLeft?.toBoolean(),
    attachPublicKey = attachPublicKey?.toBoolean(),
    sign = sign?.toBoolean(),
    pgpScheme = PackageType.enumOf(pgpScheme),
    promptPin = promptPin?.toBoolean(),
    stickyLabels = stickyLabels?.toBoolean(),
    confirmLink = confirmLink?.toBoolean(),
    mobileSettings = mobileSettings?.toSettings()
)

internal fun MailSettingsEntity.fromEntity() = MailSettings(
    userId = userId,
    displayName = displayName,
    signature = signature,
    autoSaveContacts = autoSaveContacts?.toBoolean(),
    composerMode = ComposerMode.enumOf(composerMode),
    messageButtons = MessageButtons.enumOf(messageButtons),
    showImages = ShowImage.enumOf(showImages),
    showMoved = ShowMoved.enumOf(showMoved),
    viewMode = ViewMode.enumOf(viewMode),
    viewLayout = ViewLayout.enumOf(viewLayout),
    swipeLeft = SwipeAction.enumOf(swipeLeft),
    swipeRight = SwipeAction.enumOf(swipeRight),
    shortcuts = shortcuts?.toBoolean(),
    pmSignature = PMSignature.enumOf(pmSignature),
    numMessagePerPage = numMessagePerPage,
    autoDeleteSpamAndTrashDays = autoDeleteSpamAndTrashDays,
    draftMimeType = MimeType.enumOf(draftMimeType),
    receiveMimeType = MimeType.enumOf(receiveMimeType),
    showMimeType = MimeType.enumOf(showMimeType),
    enableFolderColor = enableFolderColor?.toBoolean(),
    inheritParentFolderColor = inheritParentFolderColor?.toBoolean(),
    rightToLeft = rightToLeft?.toBoolean(),
    attachPublicKey = attachPublicKey?.toBoolean(),
    sign = sign?.toBoolean(),
    pgpScheme = PackageType.enumOf(pgpScheme),
    promptPin = promptPin?.toBoolean(),
    stickyLabels = stickyLabels?.toBoolean(),
    confirmLink = confirmLink?.toBoolean(),
    mobileSettings = mobileSettingsEntity?.toToolbarSettings()
)

private fun MailMobileSettingsEntity.toToolbarSettings(): MobileSettings {
    val listActions = listToolbar?.toSetting()
    val messageActions = messageToolbar?.toSetting()
    val conversationActions = conversationToolbar?.toSetting()

    return MobileSettings(listActions, messageActions, conversationActions)
}

private fun MobileSettingsResponse.toSettings(): MobileSettings {
    return MobileSettings(
        listToolbar = listToolbar?.toSettings(),
        messageToolbar = messageToolbar?.toSettings(),
        conversationToolbar = conversationToolbar?.toSettings()
    )
}

private fun ToolbarSettingsResponse.toSettings(): ActionsToolbarSetting {
    return ActionsToolbarSetting(
        this.isCustom,
        this.actions?.map { ToolbarAction.enumOf(it) }
    )
}

private fun MobileSettings?.toEntity(): MailMobileSettingsEntity {
    return MailMobileSettingsEntity(
        listToolbar = this?.listToolbar?.toActionsToolbarEntity(),
        messageToolbar = this?.messageToolbar?.toActionsToolbarEntity(),
        conversationToolbar = this?.conversationToolbar?.toActionsToolbarEntity()
    )
}

private fun ActionsToolbarEntity.toSetting(): ActionsToolbarSetting {
    return ActionsToolbarSetting(
        this.isCustom, this.actions?.map { ToolbarAction.enumOf(it) }
    )
}

private fun ActionsToolbarSetting?.toActionsToolbarEntity(): ActionsToolbarEntity {
    return ActionsToolbarEntity(
        this?.isCustom,
        this?.actions?.map { it.value }
    )
}
