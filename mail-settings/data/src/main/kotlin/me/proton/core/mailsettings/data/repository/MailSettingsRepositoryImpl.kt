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

package me.proton.core.mailsettings.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.data.api.MailSettingsApi
import me.proton.core.mailsettings.data.api.request.UpdateAttachPublicKeyRequest
import me.proton.core.mailsettings.data.api.request.UpdateAutoSaveContactsRequest
import me.proton.core.mailsettings.data.api.request.UpdateComposerModeRequest
import me.proton.core.mailsettings.data.api.request.UpdateConfirmLinkRequest
import me.proton.core.mailsettings.data.api.request.UpdateDisplayNameRequest
import me.proton.core.mailsettings.data.api.request.UpdateEnableFolderColorRequest
import me.proton.core.mailsettings.data.api.request.UpdateInheritFolderColorRequest
import me.proton.core.mailsettings.data.api.request.UpdateMessageButtonsRequest
import me.proton.core.mailsettings.data.api.request.UpdateMimeTypeRequest
import me.proton.core.mailsettings.data.api.request.UpdatePGPSchemeRequest
import me.proton.core.mailsettings.data.api.request.UpdatePMSignatureRequest
import me.proton.core.mailsettings.data.api.request.UpdatePromptPinRequest
import me.proton.core.mailsettings.data.api.request.UpdateRightToLeftRequest
import me.proton.core.mailsettings.data.api.request.UpdateShowImagesRequest
import me.proton.core.mailsettings.data.api.request.UpdateShowMovedRequest
import me.proton.core.mailsettings.data.api.request.UpdateSignRequest
import me.proton.core.mailsettings.data.api.request.UpdateSignatureRequest
import me.proton.core.mailsettings.data.api.request.UpdateStickyLabelsRequest
import me.proton.core.mailsettings.data.api.request.UpdateSwipeLeftRequest
import me.proton.core.mailsettings.data.api.request.UpdateSwipeRightRequest
import me.proton.core.mailsettings.data.api.request.UpdateViewLayoutRequest
import me.proton.core.mailsettings.data.api.request.UpdateViewModeRequest
import me.proton.core.mailsettings.data.api.response.SingleMailSettingsResponse
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.extension.fromEntity
import me.proton.core.mailsettings.data.extension.fromResponse
import me.proton.core.mailsettings.data.extension.toEntity
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
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.toInt

class MailSettingsRepositoryImpl(
    db: MailSettingsDatabase,
    private val apiProvider: ApiProvider
) : MailSettingsRepository {

    private val mailSettingsDao = db.mailSettingsDao()

    private val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: UserId ->
            apiProvider.get<MailSettingsApi>(key).invoke {
                getMailSettings().mailSettings.fromResponse(key)
            }.valueOrThrow
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> observeByUserId(key) },
            writer = { _, input -> insertOrUpdate(input) },
            delete = { key -> delete(key) },
            deleteAll = { deleteAll() }
        )
    ).build()

    private fun observeByUserId(userId: UserId): Flow<MailSettings?> =
        mailSettingsDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(settings: MailSettings) =
        mailSettingsDao.insertOrUpdate(settings.toEntity())

    private suspend fun delete(userId: UserId) =
        mailSettingsDao.delete(userId)

    private suspend fun deleteAll() =
        mailSettingsDao.deleteAll()

    override fun getMailSettingsFlow(userId: UserId, refresh: Boolean): Flow<DataResult<MailSettings>> {
        return store.stream(StoreRequest.cached(userId, refresh = refresh)).map { it.toDataResult() }
    }

    override suspend fun getMailSettings(userId: UserId, refresh: Boolean): MailSettings {
        return if (refresh) store.fresh(userId) else store.get(userId)
    }

    override suspend fun updateMailSettings(mailSettings: MailSettings) {
        insertOrUpdate(mailSettings)
    }

    private suspend fun updateProperty(
        userId: UserId,
        updateApiCall: suspend MailSettingsApi.() -> SingleMailSettingsResponse
    ): MailSettings {
        return apiProvider.get<MailSettingsApi>(userId).invoke {
            val response = updateApiCall(this)
            val updatedMailSettings = response.mailSettings.fromResponse(userId)
            insertOrUpdate(updatedMailSettings)
            getMailSettings(userId)
        }.valueOrThrow
    }

    override suspend fun updateDisplayName(userId: UserId, displayName: String) = updateProperty(userId) {
        updateDisplayName(UpdateDisplayNameRequest(displayName))
    }

    override suspend fun updateSignature(userId: UserId, signature: String) = updateProperty(userId) {
        updateSignature(UpdateSignatureRequest(signature))
    }

    override suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean) = updateProperty(userId) {
        updateAutoSaveContacts(UpdateAutoSaveContactsRequest(autoSaveContacts.toInt()))
    }

    override suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode) = updateProperty(userId) {
        updateComposerMode(UpdateComposerModeRequest(composerMode.value))
    }

    override suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons) = updateProperty(userId) {
        updateMessageButtons(UpdateMessageButtonsRequest(messageButtons.value))
    }

    override suspend fun updateShowImages(userId: UserId, showImage: ShowImage) = updateProperty(userId) {
        updateShowImages(UpdateShowImagesRequest(showImage.value))
    }

    override suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved) = updateProperty(userId) {
        updateShowMoved(UpdateShowMovedRequest(showMoved.value))
    }

    override suspend fun updateViewMode(userId: UserId, viewMode: ViewMode) = updateProperty(userId) {
        updateViewMode(UpdateViewModeRequest(viewMode.value))
    }

    override suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout) = updateProperty(userId) {
        updateViewLayout(UpdateViewLayoutRequest(viewLayout.value))
    }

    override suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction) = updateProperty(userId) {
        updateSwipeLeft(UpdateSwipeLeftRequest(swipeAction.value))
    }

    override suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction) = updateProperty(userId) {
        updateSwipeRight(UpdateSwipeRightRequest(swipeAction.value))
    }

    override suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature) = updateProperty(userId) {
        updatePMSignature(UpdatePMSignatureRequest(pmSignature.value))
    }

    override suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType) = updateProperty(userId) {
        updateDraftMimeType(UpdateMimeTypeRequest(mimeType.value))
    }

    override suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType) = updateProperty(userId) {
        updateReceiveMimeType(UpdateMimeTypeRequest(mimeType.value))
    }

    override suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType) = updateProperty(userId) {
        updateShowMimeType(UpdateMimeTypeRequest(mimeType.value))
    }

    override suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean) = updateProperty(userId) {
        updateRightToLeft(UpdateRightToLeftRequest(rightToLeft.toInt()))
    }

    override suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean) = updateProperty(userId) {
        updateAttachPublicKey(UpdateAttachPublicKeyRequest(attachPublicKey.toInt()))
    }

    override suspend fun updateSign(userId: UserId, sign: Boolean) = updateProperty(userId) {
        updateSign(UpdateSignRequest(sign.toInt()))
    }

    override suspend fun updatePGPScheme(userId: UserId, packageType: PackageType) = updateProperty(userId) {
        updatePGPScheme(UpdatePGPSchemeRequest(packageType.type))
    }

    override suspend fun updatePromptPin(userId: UserId, promptPin: Boolean) = updateProperty(userId) {
        updatePromptPin(UpdatePromptPinRequest(promptPin.toInt()))
    }

    override suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean) = updateProperty(userId) {
        updateStickyLabels(UpdateStickyLabelsRequest(stickyLabels.toInt()))
    }

    override suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean) = updateProperty(userId) {
        updateConfirmLink(UpdateConfirmLinkRequest(confirmLinks.toInt()))
    }

    override suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean) = updateProperty(userId) {
        updateInheritFolderColor(UpdateInheritFolderColorRequest(inherit.toInt()))
    }

    override suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean) = updateProperty(userId) {
        updateEnableFolderColor(UpdateEnableFolderColorRequest(enable.toInt()))
    }
}
