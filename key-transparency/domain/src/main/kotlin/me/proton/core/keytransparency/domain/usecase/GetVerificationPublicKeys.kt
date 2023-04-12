package me.proton.core.keytransparency.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.publicKey
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

internal class GetVerificationPublicKeys @Inject constructor(
    private val cryptoContext: CryptoContext
) {
    operator fun invoke(userAddress: UserAddress): List<Armored> {
        return userAddress.keys.filter { it.privateKey.isActive && it.privateKey.canVerify }.map {
            it.privateKey.publicKey(cryptoContext).key
        }
    }

    operator fun invoke(publicAddress: PublicAddress): List<Armored> {
        return publicAddress.keys
            .filter { it.publicKey.canVerify && it.publicKey.isActive }
            .map {
                it.publicKey.key
            }
    }
}
