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

package me.proton.core.crypto.android.srp

import java.math.BigInteger
import kotlin.math.min

/**
 * Handy BigInteger to ByteArray calculator, needed for password calculations.
 * @author Dino Kadrikj.
 */
class BigIntegerCalculator {

    fun toBI(array: ByteArray): BigInteger {
        val reversed = array.copyOf(array.size).reversedArray()
        return BigInteger(1, reversed)
    }

    fun fromBI(bitLength: Int, bi: BigInteger): ByteArray {
        val twosComp = bi.toByteArray().reversedArray()
        val output = ByteArray(bitLength / 8)
        System.arraycopy(twosComp, 0, output, 0, min(twosComp.size, output.size))
        return output
    }
}
