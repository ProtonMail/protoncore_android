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

package me.proton.core.usersettings.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.usersettings.domain.entity.Organization
import me.proton.core.usersettings.domain.entity.OrganizationKeys
import me.proton.core.usersettings.domain.entity.OrganizationSettings
import me.proton.core.usersettings.domain.entity.OrganizationSignature

interface OrganizationRepository {

    fun getOrganizationFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<Organization>>

    suspend fun getOrganization(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Organization

    @Deprecated("Organization Keys should not be needed or updated from Mobile clients.")
    fun getOrganizationKeysFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): Flow<DataResult<OrganizationKeys>>

    @Deprecated("Organization Keys should not be needed or updated from Mobile clients.")
    suspend fun getOrganizationKeys(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): OrganizationKeys

    suspend fun getOrganizationSignature(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): OrganizationSignature

    suspend fun getOrganizationSettings(
        sessionUserId: SessionUserId,
        refresh: Boolean = false
    ): OrganizationSettings

    suspend fun getOrganizationLogo(
        sessionUserId: SessionUserId,
        logoId: String
    ): ByteArray
}
