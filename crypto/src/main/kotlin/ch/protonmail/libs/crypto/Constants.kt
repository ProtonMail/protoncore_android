// Constant values
@file:SuppressLint()

package ch.protonmail.libs.crypto

import android.annotation.SuppressLint

/** Prefix for BCrypt */
const val BCRYPT_PREFIX = "$2a$10$"

const val PGP_BEGIN = "-----BEGIN PGP MESSAGE-----"
const val PGP_END = "-----END PGP MESSAGE-----"

// region algorithms
const val AES = "AES"
const val RSA = "RSA"
const val SHA_256 = "SHA-256"
// endregion
