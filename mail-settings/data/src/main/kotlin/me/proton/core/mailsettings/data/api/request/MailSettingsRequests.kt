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

package me.proton.core.mailsettings.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateDisplayNameRequest(
    @SerialName("DisplayName")
    val displayName: String
)

@Serializable
data class UpdateSignatureRequest(
    @SerialName("Signature")
    val signature: String,
)

@Serializable
data class UpdateAutoSaveContactsRequest(
    @SerialName("AutoSaveContacts")
    val autoSaveContacts: Int,
)

@Serializable
data class UpdateComposerModeRequest(
    @SerialName("ComposerMode")
    val composerMode: Int,
)

@Serializable
data class UpdateMessageButtonsRequest(
    @SerialName("MessageButtons")
    val messageButtons: Int,
)

@Serializable
data class UpdateShowImagesRequest(
    @SerialName("ShowImages")
    val showImages: Int,
)

@Serializable
data class UpdateShowMovedRequest(
    @SerialName("ShowMoved")
    val showMoved: Int,
)

@Serializable
data class UpdateViewModeRequest(
    @SerialName("ViewMode")
    val viewMode: Int,
)

@Serializable
data class UpdateViewLayoutRequest(
    @SerialName("ViewLayout")
    val viewLayout: Int,
)

@Serializable
data class UpdateSwipeLeftRequest(
    @SerialName("SwipeLeft")
    val swipeLeft: Int,
)

@Serializable
data class UpdateSwipeRightRequest(
    @SerialName("SwipeRight")
    val swipeRight: Int,
)

@Serializable
data class UpdatePMSignatureRequest(
    @SerialName("PMSignature")
    val pmSignature: Int,
)

@Serializable
data class UpdateMimeTypeRequest(
    @SerialName("MIMEType")
    val mimeType: String,
)

@Serializable
data class UpdateRightToLeftRequest(
    @SerialName("RightToLeft")
    val rightToLeft: Int,
)

@Serializable
data class UpdateAttachPublicKeyRequest(
    @SerialName("AttachPublicKey")
    val attachPublicKey: Int,
)

@Serializable
data class UpdateSignRequest(
    @SerialName("Sign")
    val sign: Int,
)

@Serializable
data class UpdatePGPSchemeRequest(
    @SerialName("PGPScheme")
    val pgpScheme: Int,
)

@Serializable
data class UpdatePromptPinRequest(
    @SerialName("PromptPin")
    val promptPin: Int,
)

@Serializable
data class UpdateStickyLabelsRequest(
    @SerialName("StickyLabels")
    val stickyLabels: Int,
)

@Serializable
data class UpdateConfirmLinkRequest(
    @SerialName("ConfirmLink")
    val confirmLink: Int,
)

@Serializable
data class UpdateInheritFolderColorRequest(
    @SerialName("InheritParentFolderColor")
    val inheritParentFolderColor: Int,
)

@Serializable
data class UpdateEnableFolderColorRequest(
    @SerialName("EnableFolderColor")
    val enableFolderColor: Int,
)

@Serializable
data class UpdateAutoDeleteSpamAndTrashDaysRequest(
    @SerialName("Days")
    val autoDeleteSpamAndTrashDays: Int
)

@Serializable
data class UpdateMobileSettingsRequest(
    @SerialName("ListToolbar")
    val listToolbar: List<String>,
    @SerialName("MessageToolbar")
    val messageToolbar: List<String>,
    @SerialName("ConversationToolbar")
    val conversationToolbar: List<String>
)

@Serializable
data class UpdateAlmostAllMailRequest(
    @SerialName("AlmostAllMail")
    val almostAllMail: Int
)
