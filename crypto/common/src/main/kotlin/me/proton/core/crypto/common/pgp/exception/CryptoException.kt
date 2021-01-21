/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.crypto.common.pgp.exception

/**
 * A generic exception indicating the problem in Cryptographic process.
 *
 * This is a generic exception type that can be thrown during the problem at any stage of the Cryptographic process,
 * including encrypting, decrypting, signing or verifying.
 */
open class CryptoException : Throwable {
    /**
     * Creates an instance of [CryptoException] without any details.
     */
    constructor()

    /**
     * Creates an instance of [CryptoException] with the specified detail [message].
     */
    constructor(message: String?) : super(message)

    /**
     * Creates an instance of [CryptoException] with the specified detail [message], and the given [cause].
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Creates an instance of [CryptoException] with the specified [cause].
     */
    constructor(cause: Throwable?) : super(cause)
}
