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
import me.proton.core.mailsettings.data.api.response.MailSettingsResponse
import me.proton.core.mailsettings.data.api.response.SingleMailSettingsResponse
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.mailsettings.data.db.dao.MailSettingsDao
import me.proton.core.mailsettings.data.extension.fromResponse
import me.proton.core.mailsettings.data.extension.toEntity
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import org.junit.Before
import org.junit.Test

class MailSettingsRepositoryTests {

    private lateinit var mailSettingsRepository: MailSettingsRepositoryImpl

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

    private val sessionId = SessionId("sessionId")
    private val userId = UserId("userId")
    private val response = MailSettingsResponse(
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
        confirmLink = 1
    )

    @Before
    fun beforeEveryTest() {
        mailSettingsRepository = MailSettingsRepositoryImpl(
            db,
            ApiProvider(apiManagerFactory, sessionProvider)
        )
    }

    @Test
    fun `mailSettingsRepository updateProperty`() = runBlockingTest {
        // GIVEN
        coEvery { mailSettingsApi.updateComposerMode(any()) } returns SingleMailSettingsResponse(
            response
        )
        every { mailSettingsDao.observeByUserId(any()) } returns flowOf(
            response.fromResponse(userId).toEntity()
        )
        // WHEN
        mailSettingsRepository.updateComposerMode(userId, ComposerMode.Maximized)
        // THEN
        coVerify { mailSettingsDao.insertOrUpdate(any()) }
        verify { mailSettingsDao.observeByUserId(any()) }
    }
}
