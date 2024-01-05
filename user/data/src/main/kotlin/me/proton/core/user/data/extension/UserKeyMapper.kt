package me.proton.core.user.data.extension

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptOrElse
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.response.UserKeyResponse
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.data.entity.UserKeyEntity
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.util.kotlin.toBooleanOrFalse

internal fun UserKeyResponse.toUserKey(userId: UserId) = UserKey(
    userId = userId,
    version = version,
    activation = activation,
    active = active.toBooleanOrFalse(),
    recoverySecret = recoverySecret,
    recoverySecretSignature = recoverySecretSignature,
    keyId = KeyId(id),
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = primary.toBooleanOrFalse(),
        isActive = false,
        passphrase = null,
    )
)

internal fun UserKey.toEntity(
    keyStoreCrypto: KeyStoreCrypto
) = UserKeyEntity(
    userId = userId,
    keyId = keyId,
    version = version,
    privateKey = privateKey.key,
    isPrimary = privateKey.isPrimary,
    isUnlockable = privateKey.isActive,
    activation = activation,
    active = active,
    recoverySecret = recoverySecret?.encrypt(keyStoreCrypto),
    recoverySecretSignature = recoverySecretSignature?.encrypt(keyStoreCrypto)
)

internal fun UserKeyEntity.toUserKey(passphrase: EncryptedByteArray?, keyStoreCrypto: KeyStoreCrypto) = UserKey(
    userId = userId,
    keyId = keyId,
    version = version,
    activation = activation,
    active = active,
    recoverySecret = recoverySecret?.decryptOrElse(keyStoreCrypto) { null },
    recoverySecretSignature = recoverySecretSignature?.decryptOrElse(keyStoreCrypto) { null },
    privateKey = PrivateKey(
        key = privateKey,
        isPrimary = isPrimary,
        // If active is null (unknown during offline migration), we rely on isUnlockable.
        // active will be null until we refresh/update the UserKey from remote.
        isActive = (active ?: true) && isUnlockable && passphrase != null,
        passphrase = passphrase
    )
)

internal fun List<UserKey>.toEntityList(keyStoreCrypto: KeyStoreCrypto) =
    map { it.toEntity(keyStoreCrypto) }

internal fun List<UserKeyEntity>.toUserKeyList(passphrase: EncryptedByteArray?, keyStoreCrypto: KeyStoreCrypto) =
    map { it.toUserKey(passphrase, keyStoreCrypto) }
