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

package me.proton.core.auth.domain.crypto

import java.math.BigInteger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Semaphore

/**
 * Generates array of primes based on the worker threads.
 *
 * @author Dino Kadrikj.
 */
class PrimeGenerator @JvmOverloads constructor(
    private val workerThreads: Int = Runtime.getRuntime().availableProcessors()
) {

    /**
     * Returns array of primes as [BigInteger].
     * @param bitLength the length of the returned prime [BigInteger]
     * @param n number of wanted primes
     */
    fun generatePrimes(bitLength: Int, n: Int): Array<BigInteger?>? {
        val primeChannel: BlockingQueue<BigInteger> = ArrayBlockingQueue(n)
        val primesNeeded = Semaphore(n)

        // The workers will race to find primes.
        val workers = arrayOfNulls<Thread>(workerThreads)
        for (i in 0 until workerThreads) {
            workers[i] = Thread(PrimeWorker(primesNeeded, primeChannel, bitLength))
            workers[i]!!.start()
        }
        val primes = arrayOfNulls<BigInteger>(n)
        for (i in 0 until n) {
            try {
                primes[i] = primeChannel.take()
            } catch (e: InterruptedException) {
                return null
            }
        }

        // They will automatically stop generating primes.
        for (i in 0 until workerThreads) {
            workers[i]!!.priority = Thread.MIN_PRIORITY
        }
        return primes
    }

}
