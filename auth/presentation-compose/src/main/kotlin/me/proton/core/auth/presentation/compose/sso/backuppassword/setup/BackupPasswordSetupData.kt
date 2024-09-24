/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

import coil.request.ImageRequest
import me.proton.core.crypto.common.pgp.Armored

/**
 * @param organizationIcon Anything that is accepted by [ImageRequest.Builder.data].
 */
public data class BackupPasswordSetupData(
    val organizationAdminEmail: String? = null,
    val organizationIcon: Any? = null,
    val organizationName: String? = null,
    val organizationPublicKey: Armored? = null
)
