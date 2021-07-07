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

package me.proton.core.mailsettings.data.api

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
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface MailSettingsApi : BaseRetrofitApi {

    @GET("mail/v4/settings")
    suspend fun getMailSettings(): SingleMailSettingsResponse

    @PUT("mail/v4/settings/display")
    suspend fun updateDisplayName(@Body request: UpdateDisplayNameRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/signature")
    suspend fun updateSignature(@Body request: UpdateSignatureRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/autocontacts")
    suspend fun updateAutoSaveContacts(@Body request: UpdateAutoSaveContactsRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/composermode")
    suspend fun updateComposerMode(@Body request: UpdateComposerModeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/messagebuttons")
    suspend fun updateMessageButtons(@Body request: UpdateMessageButtonsRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/images")
    suspend fun updateShowImages(@Body request: UpdateShowImagesRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/moved")
    suspend fun updateShowMoved(@Body request: UpdateShowMovedRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/viewmode")
    suspend fun updateViewMode(@Body request: UpdateViewModeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/viewlayout")
    suspend fun updateViewLayout(@Body request: UpdateViewLayoutRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/swipeleft")
    suspend fun updateSwipeLeft(@Body request: UpdateSwipeLeftRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/swiperight")
    suspend fun updateSwipeRight(@Body request: UpdateSwipeRightRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/pmsignature")
    suspend fun updatePMSignature(@Body request: UpdatePMSignatureRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/drafttype")
    suspend fun updateDraftMimeType(@Body request: UpdateMimeTypeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/receivetype")
    suspend fun updateReceiveMimeType(@Body request: UpdateMimeTypeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/showtype")
    suspend fun updateShowMimeType(@Body request: UpdateMimeTypeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/righttoleft")
    suspend fun updateRightToLeft(@Body request: UpdateRightToLeftRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/attachpublic")
    suspend fun updateAttachPublicKey(@Body request: UpdateAttachPublicKeyRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/sign")
    suspend fun updateSign(@Body request: UpdateSignRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/pgpscheme")
    suspend fun updatePGPScheme(@Body request: UpdatePGPSchemeRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/promptpin")
    suspend fun updatePromptPin(@Body request: UpdatePromptPinRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/stickylabels")
    suspend fun updateStickyLabels(@Body request: UpdateStickyLabelsRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/confirmlink")
    suspend fun updateConfirmLink(@Body request: UpdateConfirmLinkRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/inheritparentfoldercolor")
    suspend fun updateInheritFolderColor(@Body request: UpdateInheritFolderColorRequest): SingleMailSettingsResponse

    @PUT("mail/v4/settings/enablefoldercolor")
    suspend fun updateEnableFolderColor(@Body request: UpdateEnableFolderColorRequest): SingleMailSettingsResponse
}
