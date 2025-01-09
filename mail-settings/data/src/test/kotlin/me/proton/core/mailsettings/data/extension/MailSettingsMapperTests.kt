/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.mailsettings.data.extension

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import me.proton.core.mailsettings.data.api.response.MailSettingsResponse
import me.proton.core.mailsettings.data.api.response.ToolbarSettingsResponse
import me.proton.core.mailsettings.data.testdata.MailSettingsTestData
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PMSignature
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.mailsettings.domain.entity.ShowImage
import me.proton.core.mailsettings.domain.entity.ShowMoved
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ToolbarAction
import me.proton.core.mailsettings.domain.entity.ActionsToolbarSetting
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MailSettingsMapperTests {

    private val userId = UserId("userId")
    private lateinit var response: MailSettingsResponse
    private lateinit var expected: MailSettings

    @Before
    fun beforeEveryTest() {
        response = MailSettingsResponse(
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
            mobileSettings = MailSettingsTestData.mobileSettingsResponse
        )
        expected = MailSettings(
            userId = userId,
            displayName = null,
            signature = null,
            autoSaveContacts = true,
            composerMode = IntEnum(1, ComposerMode.Maximized),
            messageButtons = IntEnum(1, MessageButtons.UnreadFirst),
            showImages = IntEnum(1, ShowImage.Remote),
            showMoved = IntEnum(1, ShowMoved.Drafts),
            viewMode = IntEnum(1, ViewMode.NoConversationGrouping),
            viewLayout = IntEnum(1, ViewLayout.Row),
            swipeLeft = IntEnum(1, SwipeAction.Spam),
            swipeRight = IntEnum(1, SwipeAction.Spam),
            shortcuts = true,
            pmSignature = IntEnum(1, PMSignature.Disabled),
            numMessagePerPage = 1,
            autoDeleteSpamAndTrashDays = 30,
            draftMimeType = StringEnum("text/plain", MimeType.PlainText),
            receiveMimeType = StringEnum("text/plain", MimeType.PlainText),
            showMimeType = StringEnum("text/plain", MimeType.PlainText),
            enableFolderColor = true,
            inheritParentFolderColor = true,
            rightToLeft = true,
            attachPublicKey = true,
            sign = true,
            pgpScheme = IntEnum(1, PackageType.ProtonMail),
            promptPin = true,
            stickyLabels = true,
            confirmLink = true,
            mobileSettings = MailSettingsTestData.mobileSettings
        )
    }

    @Test
    fun `mailSettingsMapper fromResponse all known`() = runTest {
        // GIVEN
        val response = response.copy()
        val expected = expected.copy()
        // WHEN
        val actual = response.fromResponse(userId)
        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `mailSettingsMapper fromResponse autoSaveContacts false`() = runTest {
        // GIVEN
        val response = response.copy(
            autoSaveContacts = 0
        )
        val expected = expected.copy(
            autoSaveContacts = false,
        )
        // WHEN
        val actual = response.fromResponse(userId)
        // THEN
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mailSettingsMapper fromResponse autoSaveContacts unknown`() = runTest {
        // GIVEN
        val response = response.copy(
            autoSaveContacts = 1234
        )
        // WHEN
        val actual = response.fromResponse(userId)
        // THEN -> IllegalArgumentException
    }

    @Test
    fun `mailSettingsMapper fromResponse composerMode unknown`() = runTest {
        // GIVEN
        val response = response.copy(
            composerMode = 1234
        )
        val expected = expected.copy(
            composerMode = IntEnum(1234, null),
        )
        // WHEN
        val actual = response.fromResponse(userId)
        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `mailSettingsMapper fromResponse draftMimeType unknown`() = runTest {
        // GIVEN
        val response = response.copy(
            draftMimeType = "1234"
        )
        val expected = expected.copy(
            draftMimeType = StringEnum("1234", null),
        )
        // WHEN
        val actual = response.fromResponse(userId)
        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `mailSettingsMapper fromResponse toEntity fromEntity all ok`() = runTest {
        // GIVEN
        val response = response.copy()
        val expected = expected.copy()
        // WHEN
        val actual = response.fromResponse(userId).toEntity().fromEntity()
        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `mailSettingsMapper fromResponse toEntity fromEntity composerMode unknown`() = runTest {
        // GIVEN
        val response = response.copy(
            composerMode = 1234
        )
        val expected = expected.copy(
            composerMode = IntEnum(1234, null),
        )
        // WHEN
        val actual = response.fromResponse(userId).toEntity().fromEntity()
        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `mailSettingsMapper fromResponse toEntity fromEntity toolbarAction unknown`() = runTest {
        // GIVEN
        val response = response.copy(
            mobileSettings = MailSettingsTestData.mobileSettingsResponse.copy(
                listToolbar = ToolbarSettingsResponse(true, listOf("unexpected", "move"))
            )
        )
        val expected = expected.copy(
            mobileSettings = MailSettingsTestData.mobileSettings.copy(
                listToolbar = ActionsToolbarSetting(
                    isCustom = true,
                    listOf(
                        StringEnum("unexpected", null),
                        StringEnum(ToolbarAction.MoveTo.value, ToolbarAction.MoveTo)
                    )
                )
            )
        )
        // WHEN
        val actual = response.fromResponse(userId).toEntity().fromEntity()
        // THEN
        assertEquals(expected, actual)
    }
}
