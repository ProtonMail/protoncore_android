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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.data.arch.toDataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import me.proton.core.mailsettings.data.api.MailSettingsApi
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.extension.fromEntity
import me.proton.core.mailsettings.data.extension.fromResponse
import me.proton.core.mailsettings.data.extension.toEntity
import me.proton.core.mailsettings.data.worker.SettingsProperty
import me.proton.core.mailsettings.data.worker.UpdateSettingsWorker
import me.proton.core.mailsettings.domain.entity.AlmostAllMail
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
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

@Suppress("TooManyFunctions", "ComplexInterface")
class MailSettingsRepositoryImpl @Inject constructor(
    db: MailSettingsDatabase,
    private val apiProvider: ApiProvider,
    private val settingsWorker: UpdateSettingsWorker.Enqueuer,
    scopeProvider: CoroutineScopeProvider
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
    ).buildProtonStore(scopeProvider)

    private fun observeByUserId(userId: UserId): Flow<MailSettings?> =
        mailSettingsDao.observeByUserId(userId).map { it?.fromEntity() }

    private suspend fun insertOrUpdate(settings: MailSettings) =
        mailSettingsDao.insertOrUpdate(settings.toEntity())

    private suspend fun delete(userId: UserId) = mailSettingsDao.delete(userId)

    private suspend fun deleteAll() = mailSettingsDao.deleteAll()

    private suspend fun updateSettingsProperty(
        userId: UserId,
        settingsProperty: SettingsProperty,
        updateProperty: suspend (MailSettings) -> MailSettings
    ): MailSettings {
        val mailSettings = getMailSettings(userId)
        val updatedMailSettings = updateProperty(mailSettings)
        insertOrUpdate(updatedMailSettings)
        settingsWorker.enqueue(userId, settingsProperty)
        return updatedMailSettings
    }

    override fun getMailSettingsFlow(userId: UserId, refresh: Boolean) =
        store.stream(StoreRequest.cached(userId, refresh = refresh)).map { it.toDataResult() }

    override suspend fun getMailSettings(userId: UserId, refresh: Boolean) =
        if (refresh) store.fresh(userId) else store.get(userId)

    override suspend fun updateMailSettings(mailSettings: MailSettings) {
        insertOrUpdate(mailSettings)
    }

    override suspend fun updateDisplayName(userId: UserId, displayName: String) =
        updateSettingsProperty(userId, SettingsProperty.DisplayName(displayName)) {
            it.copy(displayName = displayName)
        }

    override suspend fun updateSignature(userId: UserId, signature: String) =
        updateSettingsProperty(userId, SettingsProperty.Signature(signature)) {
            it.copy(signature = signature)
        }

    override suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean) =
        updateSettingsProperty(
            userId,
            SettingsProperty.AutoSaveContacts(autoSaveContacts.toInt())
        ) {
            it.copy(autoSaveContacts = autoSaveContacts)
        }

    override suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode) =
        updateSettingsProperty(userId, SettingsProperty.ComposerMode(composerMode.value)) {
            it.copy(composerMode = IntEnum(composerMode.value, composerMode))
        }

    override suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons) =
        updateSettingsProperty(userId, SettingsProperty.MessageButtons(messageButtons.value)) {
            it.copy(messageButtons = IntEnum(messageButtons.value, messageButtons))
        }

    override suspend fun updateShowImages(userId: UserId, showImage: ShowImage) =
        updateSettingsProperty(userId, SettingsProperty.ShowImages(showImage.value)) {
            it.copy(showImages = IntEnum(showImage.value, showImage))
        }

    override suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved) =
        updateSettingsProperty(userId, SettingsProperty.ShowMoved(showMoved.value)) {
            it.copy(showMoved = IntEnum(showMoved.value, showMoved))
        }

    override suspend fun updateViewMode(userId: UserId, viewMode: ViewMode) =
        updateSettingsProperty(userId, SettingsProperty.ViewMode(viewMode.value)) {
            it.copy(viewMode = IntEnum(viewMode.value, viewMode))
        }

    override suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout) =
        updateSettingsProperty(userId, SettingsProperty.ViewLayout(viewLayout.value)) {
            it.copy(viewLayout = IntEnum(viewLayout.value, viewLayout))
        }

    override suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction) =
        updateSettingsProperty(userId, SettingsProperty.SwipeLeft(swipeAction.value)) {
            it.copy(swipeLeft = IntEnum(swipeAction.value, swipeAction))
        }

    override suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction) =
        updateSettingsProperty(userId, SettingsProperty.SwipeRight(swipeAction.value)) {
            it.copy(swipeRight = IntEnum(swipeAction.value, swipeAction))
        }

    override suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature) =
        updateSettingsProperty(userId, SettingsProperty.PmSignature(pmSignature.value)) {
            it.copy(pmSignature = IntEnum(pmSignature.value, pmSignature))
        }

    override suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType) =
        updateSettingsProperty(userId, SettingsProperty.DraftMimeType(mimeType.value)) {
            it.copy(draftMimeType = StringEnum(mimeType.value, mimeType))
        }

    override suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType) =
        updateSettingsProperty(userId, SettingsProperty.ReceiveMimeType(mimeType.value)) {
            it.copy(receiveMimeType = StringEnum(mimeType.value, mimeType))
        }

    override suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType) =
        updateSettingsProperty(userId, SettingsProperty.ShowMimeType(mimeType.value)) {
            it.copy(showMimeType = StringEnum(mimeType.value, mimeType))
        }

    override suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.RightToLeft(rightToLeft.toInt())) {
            it.copy(rightToLeft = rightToLeft)
        }

    override suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.AttachPublicKey(attachPublicKey.toInt())) {
            it.copy(attachPublicKey = attachPublicKey)
        }

    override suspend fun updateSign(userId: UserId, sign: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.Sign(sign.toInt())) {
            it.copy(sign = sign)
        }

    override suspend fun updatePGPScheme(userId: UserId, packageType: PackageType) =
        updateSettingsProperty(userId, SettingsProperty.PgpScheme(packageType.type)) {
            it.copy(pgpScheme = IntEnum(packageType.type, packageType))
        }

    override suspend fun updatePromptPin(userId: UserId, promptPin: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.PromptPin(promptPin.toInt())) {
            it.copy(promptPin = promptPin)
        }

    override suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.StickyLabels(stickyLabels.toInt())) {
            it.copy(stickyLabels = stickyLabels)
        }

    override suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.ConfirmLink(confirmLinks.toInt())) {
            it.copy(confirmLink = confirmLinks)
        }

    override suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.InheritFolderColor(inherit.toInt())) {
            it.copy(inheritParentFolderColor = inherit)
        }

    override suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean) =
        updateSettingsProperty(userId, SettingsProperty.EnableFolderColor(enable.toInt())) {
            it.copy(enableFolderColor = enable)
        }

    override suspend fun updateAutoDeleteSpamAndTrashDays(userId: UserId, autoDeleteSpamAndTrashDays: Int) =
        updateSettingsProperty(userId, SettingsProperty.AutoDeleteSpamAndTrashDays(autoDeleteSpamAndTrashDays)) {
            it.copy(autoDeleteSpamAndTrashDays = autoDeleteSpamAndTrashDays)
        }

    override suspend fun updateMobileSettings(
        userId: UserId,
        listToolbarActions: List<StringEnum<ToolbarAction>>,
        messageToolbarActions: List<StringEnum<ToolbarAction>>,
        conversationToolbarActions: List<StringEnum<ToolbarAction>>
    ): MailSettings {

        fun List<StringEnum<ToolbarAction>>.stringList() = this.map { it.value }

        return updateSettingsProperty(
            userId,
            SettingsProperty.MobileSettings(
                listActions = listToolbarActions.stringList(),
                messageActions = messageToolbarActions.stringList(),
                conversationActions = conversationToolbarActions.stringList()
            )
        ) {
            val mobileSettings = MobileSettings(
                listToolbar = it.mobileSettings
                    ?.listToolbar
                    ?.copy(actions = listToolbarActions),
                messageToolbar = it.mobileSettings
                    ?.messageToolbar
                    ?.copy(actions = messageToolbarActions),
                conversationToolbar = it.mobileSettings
                    ?.conversationToolbar
                    ?.copy(actions = conversationToolbarActions)
            )

            it.copy(mobileSettings = mobileSettings)
        }
    }

    override suspend fun updateAlmostAllMail(userId: UserId, almostAllMail: AlmostAllMail) =
        updateSettingsProperty(userId, SettingsProperty.AlmostAllMail(almostAllMail.value)) {
            it.copy(almostAllMail = IntEnum(almostAllMail.value, almostAllMail))
        }
}
