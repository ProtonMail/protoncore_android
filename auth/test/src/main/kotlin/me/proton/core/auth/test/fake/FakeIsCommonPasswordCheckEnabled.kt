/*
 * Copyright (c) 2024 Proton AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.auth.test.fake

import me.proton.core.auth.domain.feature.IsCommonPasswordCheckEnabled
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class FakeIsCommonPasswordCheckEnabled(
    private val enabled: Boolean = false
) : IsCommonPasswordCheckEnabled {

    @Inject
    public constructor() : this(enabled = false)

    override fun invoke(userId: UserId?): Boolean = enabled
    override fun isLocalEnabled(): Boolean = enabled
    override fun isRemoteEnabled(userId: UserId?): Boolean = enabled
}