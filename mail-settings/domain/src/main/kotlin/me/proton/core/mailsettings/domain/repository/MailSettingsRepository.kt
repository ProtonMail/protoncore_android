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

package me.proton.core.mailsettings.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PMSignature
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.mailsettings.domain.entity.ShowImage
import me.proton.core.mailsettings.domain.entity.ShowMoved
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode

@Suppress("TooManyFunctions", "ComplexInterface")
interface MailSettingsRepository {

    /**
     * Observe [MailSettings], by [userId].
     */
    fun getMailSettingsFlow(userId: UserId, refresh: Boolean = false): Flow<DataResult<MailSettings>>

    /**
     * Get [MailSettings], by [userId].
     *
     * @see [MailSettingsRepository.getMailSettingsOrNull]
     */
    suspend fun getMailSettings(userId: UserId, refresh: Boolean = false): MailSettings

    /**
     * Update [MailSettings], locally.
     *
     * Note: This function is usually used for Events handling.
     *
     * @throws IllegalArgumentException if corresponding user doesn't exist.
     */
    suspend fun updateMailSettings(mailSettings: MailSettings)

    /**
     * Update [displayName] for [userId], remotely.
     */
    suspend fun updateDisplayName(userId: UserId, displayName: String): MailSettings

    /**
     * Update [signature] for [userId], remotely.
     */
    suspend fun updateSignature(userId: UserId, signature: String): MailSettings

    /**
     * Update [autoSaveContacts] for [userId], remotely.
     */
    suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean): MailSettings

    /**
     * Update [composerMode] for [userId], remotely.
     */
    suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode): MailSettings

    /**
     * Update [messageButtons] for [userId], remotely.
     */
    suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons): MailSettings

    /**
     * Update [showImage] for [userId], remotely.
     */
    suspend fun updateShowImages(userId: UserId, showImage: ShowImage): MailSettings

    /**
     * Update [showMoved] for [userId], remotely.
     */
    suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved): MailSettings

    /**
     * Update [viewMode] for [userId], remotely.
     */
    suspend fun updateViewMode(userId: UserId, viewMode: ViewMode): MailSettings

    /**
     * Update [viewLayout] for [userId], remotely.
     */
    suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout): MailSettings

    /**
     * Update [swipeAction] for [userId], remotely.
     */
    suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction): MailSettings

    /**
     * Update [swipeAction] for [userId], remotely.
     */
    suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction): MailSettings

    /**
     * Update [pmSignature] for [userId], remotely.
     */
    suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature): MailSettings

    /**
     * Update [mimeType] for [userId], remotely.
     */
    suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType): MailSettings

    /**
     * Update [mimeType] for [userId], remotely.
     */
    suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType): MailSettings

    /**
     * Update [mimeType] for [userId], remotely.
     */
    suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType): MailSettings

    /**
     * Update [rightToLeft] for [userId], remotely.
     */
    suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean): MailSettings

    /**
     * Update [attachPublicKey] for [userId], remotely.
     */
    suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean): MailSettings

    /**
     * Update [sign] for [userId], remotely.
     */
    suspend fun updateSign(userId: UserId, sign: Boolean): MailSettings

    /**
     * Update [packageType] for [userId], remotely.
     */
    suspend fun updatePGPScheme(userId: UserId, packageType: PackageType): MailSettings

    /**
     * Update [promptPin] for [userId], remotely.
     */
    suspend fun updatePromptPin(userId: UserId, promptPin: Boolean): MailSettings

    /**
     * Update [stickyLabels] for [userId], remotely.
     */
    suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean): MailSettings

    /**
     * Update [confirmLinks] for [userId], remotely.
     */
    suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean): MailSettings

    /**
     * Update [inherit] for [userId], remotely.
     */
    suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean): MailSettings

    /**
     * Update [enable] for [userId], remotely.
     */
    suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean): MailSettings
}

/**
 * Get [MailSettings], by [userId].
 *
 * @return [MailSettings] or `null` if it can't be returned for [userId].
 *
 * @see [MailSettingsRepository.getMailSettings]
 */
suspend fun MailSettingsRepository.getMailSettingsOrNull(
    userId: UserId,
    refresh: Boolean = true
): MailSettings? = runCatching {
    getMailSettings(userId, refresh)
}.getOrNull()
