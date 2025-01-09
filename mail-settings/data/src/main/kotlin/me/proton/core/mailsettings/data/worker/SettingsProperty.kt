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

package me.proton.core.mailsettings.data.worker

import kotlinx.serialization.Serializable

@Serializable
sealed class SettingsProperty {

    @Serializable
    data class DisplayName(val value: String) : SettingsProperty()

    @Serializable
    data class Signature(val value: String) : SettingsProperty()

    @Serializable
    data class AutoSaveContacts(val value: Int) : SettingsProperty()

    @Serializable
    data class ComposerMode(val value: Int) : SettingsProperty()

    @Serializable
    data class MessageButtons(val value: Int) : SettingsProperty()

    @Serializable
    data class ShowImages(val value: Int) : SettingsProperty()

    @Serializable
    data class ShowMoved(val value: Int) : SettingsProperty()

    @Serializable
    data class ViewMode(val value: Int) : SettingsProperty()

    @Serializable
    data class ViewLayout(val value: Int) : SettingsProperty()

    @Serializable
    data class SwipeLeft(val value: Int) : SettingsProperty()

    @Serializable
    data class SwipeRight(val value: Int) : SettingsProperty()

    @Serializable
    data class DraftMimeType(val value: String) : SettingsProperty()

    @Serializable
    data class ReceiveMimeType(val value: String) : SettingsProperty()

    @Serializable
    data class ShowMimeType(val value: String) : SettingsProperty()

    @Serializable
    data class RightToLeft(val value: Int) : SettingsProperty()

    @Serializable
    data class AttachPublicKey(val value: Int) : SettingsProperty()

    @Serializable
    data class PmSignature(val value: Int) : SettingsProperty()

    @Serializable
    data class Sign(val value: Int) : SettingsProperty()

    @Serializable
    data class PgpScheme(val value: Int) : SettingsProperty()

    @Serializable
    data class PromptPin(val value: Int) : SettingsProperty()

    @Serializable
    data class StickyLabels(val value: Int) : SettingsProperty()

    @Serializable
    data class ConfirmLink(val value: Int) : SettingsProperty()

    @Serializable
    data class InheritFolderColor(val value: Int) : SettingsProperty()

    @Serializable
    data class EnableFolderColor(val value: Int) : SettingsProperty()

    @Serializable
    data class AutoDeleteSpamAndTrashDays(val value: Int) : SettingsProperty()

    @Serializable
    data class MobileSettings(
        val listActions: List<String>,
        val messageActions: List<String>,
        val conversationActions: List<String>
    ) : SettingsProperty()
}
