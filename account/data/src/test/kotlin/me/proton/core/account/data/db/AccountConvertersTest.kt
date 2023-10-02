/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.account.data.db

import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AccountConvertersTest {
    private lateinit var converters: AccountConverters

    @Before
    fun setUp() {
        converters = AccountConverters()
    }

    @Test
    fun fromSessionStateToStringConverter() {
        SessionState.values().forEach { sessionState ->
            assertEquals(
                sessionState.name,
                converters.fromSessionStateToString(sessionState)
            )
        }
        assertEquals(
            null,
            converters.fromSessionStateToString(null)
        )
    }

    @Test
    fun fromStringToSessionStateConverter() {
        SessionState.values().forEach { sessionState ->
            assertEquals(
                sessionState,
                converters.fromStringToSessionState(sessionState.name)
            )
        }
        assertEquals(
            null,
            converters.fromStringToSessionState(null)
        )
    }

    @Test
    fun fromStringToAccountStateConverter() {
        AccountState.values().forEach { accountState ->
            assertEquals(
                accountState,
                converters.fromStringToAccountState(accountState.name)
            )
        }

        assertEquals(
            null,
            converters.fromStringToAccountState(null)
        )
    }

    @Test
    fun fromAccountStateToStringConverter() {
        AccountState.values().forEach { accountState ->
            assertEquals(
                accountState.name,
                converters.fromAccountStateToString(accountState)
            )
        }

        assertEquals(
            null,
            converters.fromAccountStateToString(null)
        )
    }

    @Test
    fun fromAccountTypeToStringConverter() {
        AccountType.values().forEach { accountType ->
            assertEquals(
                accountType.name,
                converters.fromAccountTypeToString(accountType)
            )
        }

        assertEquals(
            null,
            converters.fromAccountTypeToString(null)
        )
    }

    @Test
    fun fromStringToAccountTypeConverter() {
        AccountType.values().forEach { accountType ->
            assertEquals(
                accountType,
                converters.fromStringToAccountType(accountType.name)
            )
        }

        assertEquals(
            null,
            converters.fromStringToAccountType(null)
        )
    }
}
