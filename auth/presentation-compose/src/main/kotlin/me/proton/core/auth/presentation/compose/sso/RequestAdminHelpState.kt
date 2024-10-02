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

package me.proton.core.auth.presentation.compose.sso

public sealed class RequestAdminHelpState(
    public open val data: RequestAdminHelpData
) {
    public data class Idle(
        public override val data: RequestAdminHelpData
    ) : RequestAdminHelpState(data)

    public data class Loading(
        public override val data: RequestAdminHelpData
    ) : RequestAdminHelpState(data)

    public data class AdminHelpHelpRequested(
        public override val data: RequestAdminHelpData
    ) : RequestAdminHelpState(data)

    public data class Error(
        public override val data: RequestAdminHelpData,
        val cause: Throwable
    ) : RequestAdminHelpState(data)
}
