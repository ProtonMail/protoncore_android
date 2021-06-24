/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.crypto.android.pgp

import com.proton.gopenpgp.crypto.EncryptSplitResult
import com.proton.gopenpgp.crypto.PlainMessageReader
import com.proton.gopenpgp.crypto.Reader
import com.proton.gopenpgp.crypto.Writer
import com.proton.gopenpgp.helper.MobileReadResult
import com.proton.gopenpgp.helper.MobileReader
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

internal fun OutputStream.writer() = Writer { buffer ->
    write(buffer)
    buffer.size.toLong()
}

internal fun InputStream.reader() = Reader { buffer ->
    read(buffer, 0, buffer.size).toLong()
}

internal fun InputStream.mobileReader() = ByteArray(GOpenPGPCrypto.DEFAULT_BUFFER_SIZE).let { buffer ->
    MobileReader { maxLen ->
        val len = min(maxLen.toInt(), GOpenPGPCrypto.DEFAULT_BUFFER_SIZE)
        val read = read(buffer, 0, len)
        val eof = read <= 0
        MobileReadResult(read.toLong(), eof, buffer)
    }
}

internal fun Reader.copyTo(writer: Writer): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(GOpenPGPCrypto.DEFAULT_BUFFER_SIZE)
    var bytes = read(buffer).toInt()
    while (bytes >= 0) {
        writer.write(buffer.takeIf { bytes == GOpenPGPCrypto.DEFAULT_BUFFER_SIZE } ?: buffer.copyOf(bytes))
        bytesCopied += bytes
        bytes = read(buffer).toInt()
    }
    return bytesCopied
}

internal fun <R> EncryptSplitResult.use(block: (EncryptSplitResult) -> R): EncryptSplitResult {
    try {
        block(this)
        return this
    } finally {
        close()
    }
}

internal fun <R> PlainMessageReader.use(block: (PlainMessageReader) -> R): PlainMessageReader {
    try {
        block(this)
        return this
    } finally {
        verifySignature()
    }
}
