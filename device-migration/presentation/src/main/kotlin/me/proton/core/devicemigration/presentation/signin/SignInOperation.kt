/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.signin

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.network.domain.session.Session

public sealed interface SignInOperation

public sealed interface SignInAction : SignInOperation {
    public data class Load(val unused: Long = System.currentTimeMillis()) : SignInAction
    public data class SessionForkPulled(
        val passphrase: EncryptedByteArray?,
        val session: Session.Authenticated
    ) : SignInAction
}
