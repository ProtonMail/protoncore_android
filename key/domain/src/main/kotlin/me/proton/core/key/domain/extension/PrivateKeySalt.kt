package me.proton.core.key.domain.extension

import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKeySalt

fun List<PrivateKeySalt>.getByKeyId(keyId: KeyId): String? =
    find { it.keyId == keyId }?.keySalt?.takeIf { it.isNotEmpty() }
