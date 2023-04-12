/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.data.usecase

import com.proton.gopenpgp.ktclient.Ktclient
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.usecase.GetCurrentTime
import me.proton.core.keytransparency.domain.usecase.GetKeyTransparencyParameters
import me.proton.core.keytransparency.domain.usecase.VerifyEpoch
import javax.inject.Inject
import com.proton.gopenpgp.ktclient.Epoch as GolangEpoch

public class VerifyEpochGolangImpl @Inject constructor(
    private val getKeyTransparencyParameters: GetKeyTransparencyParameters,
    private val getCurrentTime: GetCurrentTime
) : VerifyEpoch {

    override suspend operator fun invoke(epoch: Epoch): Long {
        val parameters = getKeyTransparencyParameters()
        checkIssuer(epoch)
        return runCatching {
            Ktclient.verifyEpoch(
                epoch.toGolang(),
                parameters.certificateDomain,
                getCurrentTime()
            )
        }.getOrElse { throw KeyTransparencyException("Epoch verification failed", it) }
    }

    private fun Epoch.toGolang(): GolangEpoch {
        return GolangEpoch(
            epochId.toLong(),
            previousChainHash,
            certificateChain,
            certificateIssuer.value.toLong(),
            treeHash,
            chainHash,
            certificateTime
        )
    }

    private fun checkIssuer(epoch: Epoch) {
        checkNotNull(epoch.certificateIssuer.enum) { "Unknown certificate issuer: ${epoch.certificateIssuer.value}" }
    }
}
