package ch.protonmail.libs.crypto

import java.math.BigInteger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Semaphore
import java.security.SecureRandom
import java.util.concurrent.BlockingQueue

class PrimeGenerator @JvmOverloads constructor(private val workerThreads: Int = Runtime.getRuntime().availableProcessors()) {

    /**
     * @return [Array] of [BigInteger]
     * TODO Doc
     *
     * @throws InterruptedException
     */
    fun generatePrimes(bitLength: Int, n: Int): Array<BigInteger> {
        val primeChannel = ArrayBlockingQueue<BigInteger>(n)
        val primesNeeded = Semaphore(n)

        // The workers will race to find primes.
        val workers = arrayOfNulls<Thread>(workerThreads)
        for (i in 0 until workerThreads) {
            workers[i] = Thread(PrimeWorker(primesNeeded, primeChannel, bitLength))
            workers[i]?.start()
        }

        val primes = arrayOfNulls<BigInteger>(n)
        for (i in 0 until n) {
            primes[i] = primeChannel.take()
        }

        // They will automatically stop generating primes.
        for (i in 0 until workerThreads) {
            workers[i]?.priority = Thread.MIN_PRIORITY
        }

        return primes.requireNoNulls()
    }

}

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
