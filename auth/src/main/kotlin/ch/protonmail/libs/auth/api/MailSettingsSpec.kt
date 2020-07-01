package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.response.MailSettingsResponse

internal interface MailSettingsSpec {

    suspend fun fetchMailSettings(): MailSettingsResponse

    // suspend fun updateSignature(signature: String): ResponseBody?

    // suspend fun updateDisplayName(displayName: String): ResponseBody?

    // suspend fun updateLeftSwipe(swipeSelection: Int): ResponseBody?

    // suspend fun updateRightSwipe(swipeSelection: Int): ResponseBody?

    // suspend fun updateAutoShowImages(autoShowImages: Int): ResponseBody?
}
