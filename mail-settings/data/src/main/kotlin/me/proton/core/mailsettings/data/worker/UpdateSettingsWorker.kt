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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result.Failure
import androidx.work.ListenableWorker.Result.Retry
import androidx.work.ListenableWorker.Result.Success
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
import me.proton.core.mailsettings.data.worker.SettingsProperty.AttachPublicKey
import me.proton.core.mailsettings.data.worker.SettingsProperty.AutoSaveContacts
import me.proton.core.mailsettings.data.worker.SettingsProperty.ComposerMode
import me.proton.core.mailsettings.data.worker.SettingsProperty.ConfirmLink
import me.proton.core.mailsettings.data.worker.SettingsProperty.DisplayName
import me.proton.core.mailsettings.data.worker.SettingsProperty.DraftMimeType
import me.proton.core.mailsettings.data.worker.SettingsProperty.EnableFolderColor
import me.proton.core.mailsettings.data.worker.SettingsProperty.InheritFolderColor
import me.proton.core.mailsettings.data.worker.SettingsProperty.MessageButtons
import me.proton.core.mailsettings.data.worker.SettingsProperty.PgpScheme
import me.proton.core.mailsettings.data.worker.SettingsProperty.PmSignature
import me.proton.core.mailsettings.data.worker.SettingsProperty.PromptPin
import me.proton.core.mailsettings.data.worker.SettingsProperty.ReceiveMimeType
import me.proton.core.mailsettings.data.worker.SettingsProperty.RightToLeft
import me.proton.core.mailsettings.data.worker.SettingsProperty.ShowImages
import me.proton.core.mailsettings.data.worker.SettingsProperty.ShowMimeType
import me.proton.core.mailsettings.data.worker.SettingsProperty.ShowMoved
import me.proton.core.mailsettings.data.worker.SettingsProperty.Sign
import me.proton.core.mailsettings.data.worker.SettingsProperty.Signature
import me.proton.core.mailsettings.data.worker.SettingsProperty.StickyLabels
import me.proton.core.mailsettings.data.worker.SettingsProperty.SwipeLeft
import me.proton.core.mailsettings.data.worker.SettingsProperty.SwipeRight
import me.proton.core.mailsettings.data.worker.SettingsProperty.ViewLayout
import me.proton.core.mailsettings.data.worker.SettingsProperty.ViewMode
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.exhaustive
import me.proton.core.util.kotlin.serialize
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val KEY_INPUT_RAW_USER_ID = "keyUserId"
private const val KEY_INPUT_SETTINGS_PROPERTY_SERIALIZED = "keySettingsPropertySerialized"
private const val UPDATE_SETTING_MAX_RETRIES = 2

@HiltWorker
class UpdateSettingsWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val apiProvider: ApiProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val rawUserId = requireNotNull(inputData.getString(KEY_INPUT_RAW_USER_ID))
        val userId = UserId(rawUserId)
        val settingsProperty = requireNotNull(
            inputData.getString(KEY_INPUT_SETTINGS_PROPERTY_SERIALIZED)
        ).deserialize<SettingsProperty>()

        val apiResult = apiProvider.get<MailSettingsApi>(userId).invoke {
            updateRemoteProperty(settingsProperty)
        }

        return when (apiResult) {
            is ApiResult.Success -> Success.success()
            is ApiResult.Error -> retryOrFail()
        }
    }

    private fun retryOrFail() = if (runAttemptCount < UPDATE_SETTING_MAX_RETRIES) {
        Retry.retry()
    } else {
        Failure.failure()
    }

    @Suppress("ComplexMethod")
    private suspend fun MailSettingsApi.updateRemoteProperty(
        property: SettingsProperty
    ): SingleMailSettingsResponse = when (property) {
        is AttachPublicKey -> updateAttachPublicKey(UpdateAttachPublicKeyRequest(property.value))
        is AutoSaveContacts -> updateAutoSaveContacts(UpdateAutoSaveContactsRequest(property.value))
        is ComposerMode -> updateComposerMode(UpdateComposerModeRequest(property.value))
        is ConfirmLink -> updateConfirmLink(UpdateConfirmLinkRequest(property.value))
        is DisplayName -> updateDisplayName(UpdateDisplayNameRequest(property.value))
        is DraftMimeType -> updateDraftMimeType(UpdateMimeTypeRequest(property.value))
        is EnableFolderColor -> updateEnableFolderColor(UpdateEnableFolderColorRequest(property.value))
        is InheritFolderColor -> updateInheritFolderColor(UpdateInheritFolderColorRequest(property.value))
        is MessageButtons -> updateMessageButtons(UpdateMessageButtonsRequest(property.value))
        is PgpScheme -> updatePGPScheme(UpdatePGPSchemeRequest(property.value))
        is PmSignature -> updatePMSignature(UpdatePMSignatureRequest(property.value))
        is PromptPin -> updatePromptPin(UpdatePromptPinRequest(property.value))
        is ReceiveMimeType -> updateReceiveMimeType(UpdateMimeTypeRequest(property.value))
        is RightToLeft -> updateRightToLeft(UpdateRightToLeftRequest(property.value))
        is ShowImages -> updateShowImages(UpdateShowImagesRequest(property.value))
        is ShowMimeType -> updateShowMimeType(UpdateMimeTypeRequest(property.value))
        is ShowMoved -> updateShowMoved(UpdateShowMovedRequest(property.value))
        is Sign -> updateSign(UpdateSignRequest(property.value))
        is Signature -> updateSignature(UpdateSignatureRequest(property.value))
        is StickyLabels -> updateStickyLabels(UpdateStickyLabelsRequest(property.value))
        is SwipeLeft -> updateSwipeLeft(UpdateSwipeLeftRequest(property.value))
        is SwipeRight -> updateSwipeRight(UpdateSwipeRightRequest(property.value))
        is ViewLayout -> updateViewLayout(UpdateViewLayoutRequest(property.value))
        is ViewMode -> updateViewMode(UpdateViewModeRequest(property.value))
    }.exhaustive

    class Enqueuer @Inject constructor(
        private val workManager: WorkManager
    ) {

        fun enqueue(
            userId: UserId,
            settingsProperty: SettingsProperty
        ): ListenableFuture<WorkInfo> {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val updateSettingsRequest = OneTimeWorkRequestBuilder<UpdateSettingsWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        KEY_INPUT_RAW_USER_ID to userId.id,
                        KEY_INPUT_SETTINGS_PROPERTY_SERIALIZED to settingsProperty.serialize(),
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "updateSettingsWork-${userId.id}-${settingsProperty.javaClass.simpleName}",
                ExistingWorkPolicy.REPLACE,
                updateSettingsRequest
            )
            return workManager.getWorkInfoById(updateSettingsRequest.id)
        }
    }

}
