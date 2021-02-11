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

package me.proton.core.crypto.common.pgp.helper

import java.math.BigInteger
import java.security.SecureRandom
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Semaphore

/**
 * Runnable that calculates a prime based on needed primes and length.
 */
class PrimeWorker internal constructor(
    private val primesNeeded: Semaphore,
    private val primeChannel: BlockingQueue<BigInteger>,
    private val bitLength: Int
) : Runnable {

    override fun run() {
        var prime: BigInteger
        do {
            prime = BigInteger.probablePrime(bitLength, SecureRandom())
        } while (primesNeeded.tryAcquire() && primeChannel.offer(prime))
    }

}
