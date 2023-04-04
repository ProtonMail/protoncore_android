/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.crypto.common.srp

/**
 * Type alias for a base64 encoded challenge.
 */
typealias Based64Challenge = String;

/**
 * Interface for SRP challenges.
 */
interface SrpChallenge {

    //argon2 preimage challenge
    fun argon2PreimageChallenge(challenge: Based64Challenge): String;

    // ecdlp challenge
    fun ecdlpChallenge(challenge: Based64Challenge): String;
}