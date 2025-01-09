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

package me.proton.core.mailsettings.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleMailSettingsResponse(
    @SerialName("MailSettings")
    val mailSettings: MailSettingsResponse
)

@Serializable
data class MailSettingsResponse(
    @SerialName("DisplayName")
    val displayName: String?,
    @SerialName("Signature")
    val signature: String?,
    @SerialName("AutoSaveContacts")
    val autoSaveContacts: Int?,
    @SerialName("ComposerMode")
    val composerMode: Int?,
    @SerialName("MessageButtons")
    val messageButtons: Int?,
    @SerialName("ShowImages")
    val showImages: Int?,
    @SerialName("ShowMoved")
    val showMoved: Int?,
    @SerialName("ViewMode")
    val viewMode: Int?,
    @SerialName("ViewLayout")
    val viewLayout: Int?,
    @SerialName("SwipeLeft")
    val swipeLeft: Int?,
    @SerialName("SwipeRight")
    val swipeRight: Int?,
    @SerialName("Shortcuts")
    val shortcuts: Int?,
    @SerialName("PMSignature")
    val pmSignature: Int?,
    @SerialName("NumMessagePerPage")
    val numMessagePerPage: Int?,
    @SerialName("AutoDeleteSpamAndTrashDays")
    val autoDeleteSpamAndTrashDays: Int?,
    @SerialName("DraftMIMEType")
    val draftMimeType: String?,
    @SerialName("ReceiveMIMEType")
    val receiveMimeType: String?,
    @SerialName("ShowMIMEType")
    val showMimeType: String?,
    @SerialName("EnableFolderColor")
    val enableFolderColor: Int?,
    @SerialName("InheritParentFolderColor")
    val inheritParentFolderColor: Int?,
    @SerialName("RightToLeft")
    val rightToLeft: Int?,
    @SerialName("AttachPublicKey")
    val attachPublicKey: Int?,
    @SerialName("Sign")
    val sign: Int?,
    @SerialName("PGPScheme")
    val pgpScheme: Int?,
    @SerialName("PromptPin")
    val promptPin: Int?,
    @SerialName("StickyLabels")
    val stickyLabels: Int?,
    @SerialName("ConfirmLink")
    val confirmLink: Int?,
    @SerialName("MobileSettings")
    val mobileSettings: MobileSettingsResponse?
)

@Serializable
data class MobileSettingsResponse(
    @SerialName("ListToolbar")
    val listToolbar: ToolbarSettingsResponse?,
    @SerialName("MessageToolbar")
    val messageToolbar: ToolbarSettingsResponse?,
    @SerialName("ConversationToolbar")
    val conversationToolbar: ToolbarSettingsResponse?
)

@Serializable
data class ToolbarSettingsResponse(
    @SerialName("IsCustom")
    val isCustom: Boolean?,
    @SerialName("Actions")
    val actions: List<String>?
)
