/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.mailsettings.data.testdata

import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.StringEnum
import me.proton.core.mailsettings.data.api.response.MailSettingsResponse
import me.proton.core.mailsettings.data.api.response.MobileSettingsResponse
import me.proton.core.mailsettings.data.api.response.ToolbarSettingsResponse
import me.proton.core.mailsettings.data.entity.ActionsToolbarEntity
import me.proton.core.mailsettings.data.entity.MailMobileSettingsEntity
import me.proton.core.mailsettings.data.entity.MailSettingsEntity
import me.proton.core.mailsettings.domain.entity.ComposerMode
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

object MailSettingsTestData {

    private val mailMobileSettingsEntity = MailMobileSettingsEntity(
        listToolbar = ActionsToolbarEntity(
            isCustom = false,
            actions = listOf("toggle_read", "trash", "move", "label")
        ),
        messageToolbar = ActionsToolbarEntity(
            isCustom = true,
            actions = listOf("print")
        ),
        conversationToolbar = ActionsToolbarEntity(
            isCustom = true,
            actions = emptyList()
        )
    )

    val mailSettingsEntity = MailSettingsEntity(
        userId = UserId("userId"),
        displayName = "displayName",
        signature = "Signature",
        autoSaveContacts = 1,
        composerMode = ComposerMode.Maximized.value,
        messageButtons = MessageButtons.UnreadFirst.value,
        showImages = ShowImage.Remote.value,
        showMoved = ShowMoved.Drafts.value,
        viewMode = ViewMode.NoConversationGrouping.value,
        viewLayout = ViewLayout.Row.value,
        swipeLeft = SwipeAction.Spam.value,
        swipeRight = SwipeAction.Spam.value,
        shortcuts = 1,
        pmSignature = PMSignature.Disabled.value,
        numMessagePerPage = 1,
        autoDeleteSpamAndTrashDays = 30,
        draftMimeType = MimeType.PlainText.value,
        receiveMimeType = MimeType.PlainText.value,
        showMimeType = MimeType.PlainText.value,
        enableFolderColor = 1,
        inheritParentFolderColor = 1,
        rightToLeft = 1,
        attachPublicKey = 1,
        sign = 1,
        pgpScheme = PackageType.ProtonMail.type,
        promptPin = 1,
        stickyLabels = 1,
        confirmLink = 1,
        mobileSettingsEntity = mailMobileSettingsEntity,
        almostAllMail = 0
    )

    val mobileSettings = MobileSettings(
        listToolbar = ActionsToolbarSetting(
            isCustom = false,
            actions = listOf(
                StringEnum(ToolbarAction.MarkAsReadOrUnread.value, ToolbarAction.MarkAsReadOrUnread),
                StringEnum(ToolbarAction.MoveToTrash.value, ToolbarAction.MoveToTrash),
                StringEnum(ToolbarAction.MoveTo.value, ToolbarAction.MoveTo),
                StringEnum(ToolbarAction.LabelAs.value, ToolbarAction.LabelAs)
            )
        ),
        messageToolbar = ActionsToolbarSetting(
            isCustom = true,
            actions = listOf(StringEnum(ToolbarAction.Print.value, ToolbarAction.Print))
        ),
        conversationToolbar = ActionsToolbarSetting(
            isCustom = true,
            actions = emptyList()
        )
    )

    val mobileSettingsResponse = MobileSettingsResponse(
        listToolbar = ToolbarSettingsResponse(
            isCustom = false,
            actions = listOf("toggle_read", "trash", "move", "label")
        ),
        messageToolbar = ToolbarSettingsResponse(
            isCustom = true,
            actions = listOf("print")
        ),
        conversationToolbar = ToolbarSettingsResponse(
            isCustom = true,
            actions = emptyList()
        )
    )

    val apiResponse = MailSettingsResponse(
        displayName = null,
        signature = null,
        autoSaveContacts = 1,
        composerMode = 1,
        messageButtons = 1,
        showImages = 1,
        showMoved = 1,
        viewMode = 1,
        viewLayout = 1,
        swipeLeft = 1,
        swipeRight = 1,
        shortcuts = 1,
        pmSignature = 1,
        numMessagePerPage = 1,
        autoDeleteSpamAndTrashDays = 30,
        draftMimeType = "text/plain",
        receiveMimeType = "text/plain",
        showMimeType = "text/plain",
        enableFolderColor = 1,
        inheritParentFolderColor = 1,
        rightToLeft = 1,
        attachPublicKey = 1,
        sign = 1,
        pgpScheme = 1,
        promptPin = 1,
        stickyLabels = 1,
        confirmLink = 1,
        mobileSettingsResponse,
        almostAllMail = 0
    )
}
