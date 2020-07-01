package ch.protonmail.libs.crypto

enum class KeyType(private val value: String) {
    RSA("rsa"), X25519("x25519");

    override fun toString() = value
}
