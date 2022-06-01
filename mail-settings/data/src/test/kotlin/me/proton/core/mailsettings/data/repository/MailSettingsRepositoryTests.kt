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

package me.proton.core.mailsettings.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.data.api.MailSettingsApi
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.db.dao.MailSettingsDao
import me.proton.core.mailsettings.data.testdata.MailSettingsTestData
import me.proton.core.mailsettings.data.worker.SettingsProperty
import me.proton.core.mailsettings.data.worker.SettingsProperty.AutoSaveContacts
import me.proton.core.mailsettings.data.worker.SettingsProperty.DisplayName
import me.proton.core.mailsettings.data.worker.SettingsProperty.Signature
import me.proton.core.mailsettings.data.worker.UpdateSettingsWorker
import me.proton.core.mailsettings.domain.entity.ComposerMode.Normal
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType.Html
import me.proton.core.mailsettings.domain.entity.MimeType.Mixed
import me.proton.core.mailsettings.domain.entity.PMSignature.Enabled
import me.proton.core.mailsettings.domain.entity.PackageType.PgpInline
import me.proton.core.mailsettings.domain.entity.ShowImage.None
import me.proton.core.mailsettings.domain.entity.ShowMoved.Both
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.SwipeAction.Archive
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.toInt
import org.junit.Before
import org.junit.Test

class MailSettingsRepositoryTests {

    private val sessionId = SessionId("sessionId")
    private val userId = UserId("userId")

    private val mailSettingsApi = mockk<MailSettingsApi>(relaxed = true)

    private val mailSettingsDao = mockk<MailSettingsDao>(relaxed = true)
    private val db = mockk<MailSettingsDatabase>(relaxed = true) {
        every { this@mockk.mailSettingsDao() } returns mailSettingsDao
    }

    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(any()) } returns sessionId
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every {
            this@mockk.create(any(), interfaceClass = MailSettingsApi::class)
        } returns TestApiManager(mailSettingsApi)
    }
    private val settingsWorker = mockk<UpdateSettingsWorker.Enqueuer> {
        every { this@mockk.enqueue(userId, any()) } returns mockk()
    }

    private lateinit var mailSettingsRepository: MailSettingsRepositoryImpl

    @Before
    fun beforeEveryTest() {
        mailSettingsRepository = MailSettingsRepositoryImpl(
            db = db,
            apiProvider = ApiProvider(apiManagerFactory, sessionProvider, TestDispatcherProvider),
            settingsWorker = settingsWorker
        )
    }

    @Test
    fun `displayName setting is updated locally and remotely when changed`() = runBlockingTest {
        // GIVEN
        every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
            MailSettingsTestData.mailSettingsEntity
        )
        // WHEN
        mailSettingsRepository.updateDisplayName(userId, "newDisplayName")
        // THEN
        val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
            displayName = "newDisplayName"
        )
        coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
        verify { settingsWorker.enqueue(userId, DisplayName("newDisplayName")) }
    }

    @Test
    fun `signature setting is updated locally and remotely when changed`() = runBlockingTest {
        // GIVEN
        every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
            MailSettingsTestData.mailSettingsEntity
        )
        // WHEN
        mailSettingsRepository.updateSignature(userId, "new signature")
        // THEN
        val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
            signature = "new signature"
        )
        coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
        verify { settingsWorker.enqueue(userId, Signature("new signature")) }
    }

    @Test
    fun `AutoSaveContacts setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateAutoSaveContacts(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                autoSaveContacts = 0
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, AutoSaveContacts(0)) }
        }

    @Test
    fun `ComposerMode setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateComposerMode(userId, Normal)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                composerMode = Normal.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ComposerMode(0)) }
        }

    @Test
    fun `UpdateMessageButtons setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateMessageButtons(userId, MessageButtons.ReadFirst)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                messageButtons = MessageButtons.ReadFirst.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.MessageButtons(0)) }
        }

    @Test
    fun `ShowImages setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateShowImages(userId, None)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                showImages = None.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ShowImages(0)) }
        }

    @Test
    fun `ShowMoved setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateShowMoved(userId, Both)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                showMoved = Both.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ShowMoved(3)) }
        }


    @Test
    fun `ViewMode setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateViewMode(userId, ViewMode.ConversationGrouping)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                viewMode = ViewMode.ConversationGrouping.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ViewMode(0)) }
        }

    @Test
    fun `SwipeLeft setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateSwipeLeft(userId, SwipeAction.Trash)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                swipeLeft = SwipeAction.Trash.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.SwipeLeft(0)) }
        }

    @Test
    fun `SwipeRight setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateSwipeRight(userId, Archive)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                swipeRight = SwipeAction.Archive.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.SwipeRight(3)) }
        }

    @Test
    fun `PMSignature setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updatePMSignature(userId, Enabled)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                pmSignature = Enabled.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.PmSignature(0)) }
        }

    @Test
    fun `DraftMimeType setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateDraftMimeType(userId, Mixed)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                draftMimeType = Mixed.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify {
                settingsWorker.enqueue(
                    userId,
                    SettingsProperty.DraftMimeType("multipart/mixed")
                )
            }
        }

    @Test
    fun `ReceiveMimeType setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateReceiveMimeType(userId, Mixed)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                receiveMimeType = Mixed.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify {
                settingsWorker.enqueue(
                    userId,
                    SettingsProperty.ReceiveMimeType("multipart/mixed")
                )
            }
        }

    @Test
    fun `ShowMimeType setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateShowMimeType(userId, Html)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                showMimeType = Html.value
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ShowMimeType("text/html")) }
        }

    @Test
    fun `RightToLeft setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateRightToLeft(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                rightToLeft = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.RightToLeft(0)) }
        }

    @Test
    fun `AttachPublicKey setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateAttachPublicKey(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                attachPublicKey = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.AttachPublicKey(0)) }
        }

    @Test
    fun `Sign setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateSign(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                sign = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.Sign(0)) }
        }

    @Test
    fun `PGPScheme setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updatePGPScheme(userId, PgpInline)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                pgpScheme = PgpInline.type
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.PgpScheme(8)) }
        }

    @Test
    fun `PromptPin setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updatePromptPin(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                promptPin = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.PromptPin(0)) }
        }

    @Test
    fun `StickyLabels setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateStickyLabels(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                stickyLabels = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.StickyLabels(0)) }
        }

    @Test
    fun `ConfirmLink setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateConfirmLink(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                confirmLink = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.ConfirmLink(0)) }
        }

    @Test
    fun `InheritFolderColor setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateInheritFolderColor(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                inheritParentFolderColor = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.InheritFolderColor(0)) }
        }

    @Test
    fun `EnableFolderColor setting is updated locally and remotely when changed`() =
        runBlockingTest {
            // GIVEN
            every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
                MailSettingsTestData.mailSettingsEntity
            )
            // WHEN
            mailSettingsRepository.updateEnableFolderColor(userId, false)
            // THEN
            val updatedMailSettings = MailSettingsTestData.mailSettingsEntity.copy(
                enableFolderColor = false.toInt()
            )
            coVerify { mailSettingsDao.insertOrUpdate(updatedMailSettings) }
            verify { settingsWorker.enqueue(userId, SettingsProperty.EnableFolderColor(0)) }
        }
}
