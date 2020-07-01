// Cipher
@file:SuppressLint("GetInstance")

package ch.protonmail.libs.crypto.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import ch.protonmail.libs.core.preferences.PrefType
import ch.protonmail.libs.core.preferences.PrefType.*
import ch.protonmail.libs.core.preferences.get
import ch.protonmail.libs.core.preferences.set
import ch.protonmail.libs.core.utils.Android
import ch.protonmail.libs.core.utils.toBooleanOrNull
import ch.protonmail.libs.crypto.AES
import ch.protonmail.libs.crypto.RSA
import ch.protonmail.libs.crypto.SHA_256
import ch.protonmail.libs.crypto.utils.decodeBase64
import ch.protonmail.libs.crypto.utils.encodeBase64
import ch.protonmail.libs.crypto.utils.encodeBase64String
import timber.log.Timber
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import kotlin.reflect.KClass

/**
 * [SharedPreferences] that save encrypted values
 *
 * @constructor's are structured as "waterfall" bottom to top, where anyone add more details to
 * reach the primary constructor.
 * This design aim to achieve a better testability and an optimal encapsulation of information into
 * each component of the flow.
 *
 * TODO: handle `migrateToKeyStore`
 */
class SecureSharedPreferences(
    private val delegate: SharedPreferences,
    private val encrypter: Encrypter,
    private val decrypter: Decrypter
) : SharedPreferences by delegate {

    // region Constructors
    constructor(delegate: SharedPreferences, encryptCipher: Cipher, decryptCipher: Cipher) : this(
        delegate, encrypter = Encrypter(encryptCipher), decrypter = Decrypter(decryptCipher)
    )

    constructor(delegate: SharedPreferences, cryptoKeyGenerator: CryptoKeyGenerator) : this(
        delegate,
        encryptCipher = Cipher.getInstance(PREFERENCES_ENCRYPT_ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, cryptoKeyGenerator())
        },
        decryptCipher = Cipher.getInstance(PREFERENCES_ENCRYPT_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, cryptoKeyGenerator())
        }
    )

    constructor(delegate: SharedPreferences, sekrit: CharArray) : this(
        delegate, cryptoKeyGenerator = CryptoKeyGenerator(sekrit)
    )

    constructor(delegate: SharedPreferences, sekritGenerator: SekritGenerator) : this(
        delegate, sekrit = sekritGenerator()
    )

    constructor(
        delegate: SharedPreferences,
        symmetricKeyPreferences: SharedPreferences,
        asymmetricKeyEncrypter: AsymmetricKeyEncrypter,
        asymmetricKeyDecrypter: AsymmetricKeyDecrypter
    ) : this(
        delegate, sekritGenerator = SekritGenerator(
            symmetricKeyPreferences,
            asymmetricKeyEncrypter,
            asymmetricKeyDecrypter
        )
    )

    constructor(
        delegate: SharedPreferences,
        symmetricKeyPreferences: SharedPreferences,
        asymmetricEncrypterCipher: Cipher,
        asymmetricDecrypterCipher: Cipher
    ) : this(
        delegate,
        symmetricKeyPreferences = symmetricKeyPreferences,
        asymmetricKeyEncrypter = AsymmetricKeyEncrypter(asymmetricEncrypterCipher),
        asymmetricKeyDecrypter = AsymmetricKeyDecrypter(asymmetricDecrypterCipher)
    )

    constructor(
        delegate: SharedPreferences,
        symmetricKeyPreferences: SharedPreferences,
        keyPair: KeyPair
    ) : this(
        delegate,
        symmetricKeyPreferences = symmetricKeyPreferences,
        asymmetricEncrypterCipher = Cipher.getInstance(ASYMMETRIC_ENCRYPTION_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, keyPair.public)
        },
        asymmetricDecrypterCipher = Cipher.getInstance(ASYMMETRIC_ENCRYPTION_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, keyPair.private)
        }
    )

    constructor(
        delegate: SharedPreferences,
        symmetricKeyPreferences: SharedPreferences,
        keyPairProvider: KeyPairProvider,
        asymmetricKeyAlias: String = ASYMMETRIC_KEY_ALIAS
    ) : this(
        delegate,
        symmetricKeyPreferences = symmetricKeyPreferences,
        keyPair = keyPairProvider(asymmetricKeyAlias)
    )

    constructor(
        context: Context,
        delegate: SharedPreferences,
        symmetricKeyPreferences: SharedPreferences,
        keyPairGenerator: KeyPairGenerator = DefaultKeyPairGenerator,
        keyStore: KeyStore = DefaultKeyStore,
        asymmetricKeyAlias: String = ASYMMETRIC_KEY_ALIAS
    ) : this(
        delegate,
        symmetricKeyPreferences = symmetricKeyPreferences,
        keyPairProvider = KeyPairProvider(context, keyPairGenerator, keyStore),
        asymmetricKeyAlias = asymmetricKeyAlias
    )
    // endregion

    /** Wrap the [delegate]s editor inside a custom [Editor] */
    override fun edit() = Editor(delegate.edit(), encrypter)

    override fun getAll() = delegate.all.map { (key, value) ->
        key.decrypt() to value.toString().decryptAs(value!!::class)
    }.toMap()

    override fun contains(key: String?) = delegate.contains(key?.encrypt())

    override fun getBoolean(key: String, defValue: Boolean) =
        getDecrypted(key) ?: get(key, defValue)

    override fun getFloat(key: String, defValue: Float) =
        getDecrypted(key) ?: get(key, defValue)

    override fun getInt(key: String, defValue: Int) =
        getDecrypted(key) ?: get(key, defValue)

    override fun getLong(key: String, defValue: Long) =
        getDecrypted(key) ?: get(key, defValue)

    override fun getString(key: String, defValue: String) =
        getDecrypted(key) ?: get(key, defValue)

    private inline fun <reified T: Any> getDecrypted(key: String): T? {
        return delegate.get<String?>(key.encrypt())?.decryptAs<T>()
    }

    /** Editor for [SecureSharedPreferences] */
    class Editor(
        private val delegate: SharedPreferences.Editor,
        private val encrypter: Encrypter
    ) : SharedPreferences.Editor by delegate {

        override fun putBoolean(key: String, value: Boolean) = apply { putEncrypted(key, value) }

        override fun putFloat(key: String, value: Float) = apply { putEncrypted(key, value) }

        override fun putInt(key: String, value: Int) = apply { putEncrypted(key, value) }

        override fun putLong(key: String, value: Long) = apply { putEncrypted(key, value) }

        override fun putString(key: String, value: String?) = apply { putEncrypted(key, value) }

        private fun <T: Any> putEncrypted(key: String, value: T?) {
            if (value == null) {
                remove(key)
                return
            }
            delegate.putString(key.encrypt(), value.encrypt())
        }

        private fun Any.encrypt() = encrypter(this.toString())
    }

    // region Encryption components
    /** Encrypter for [SecureSharedPreferences] */
    class Encrypter(private val cipher: Cipher) {
        /**
         * @return [String] or
         * @throws [RuntimeException]
         */
        operator fun invoke(value: String?): String {
            return try {
                // String out = OpenPgp.createInstance().encryptMailboxPwd(value, SEKRIT);
                //return out;
                val bytes = value?.toByteArray() ?: ByteArray(0)
                String(cipher.doFinal(bytes).encodeBase64(Base64.NO_WRAP))
            } catch (e: Exception) {
                Timber.e(e, "Cannot encrypt: '$value'")
                throw RuntimeException(e)
            }
        }
    }

    /** Decrypter for [SecureSharedPreferences] */
    class Decrypter(private val cipher: Cipher) {

        /** @return [String] or `null` if error */
        operator fun invoke(value: String?): String? {
            return try {
                // String out = OpenPgp.createInstance().decryptMailboxPwd(value, SEKRIT);
                // return out;
                val bytes = value?.decodeBase64(Base64.NO_WRAP) ?: ByteArray(0)
                String(cipher.doFinal(bytes))
            } catch (e: Exception) {
                Timber.e(e, "Cannot decrypt: '$value'")
                value
            }
        }
    }

    /**
     * Generates a [Key] for [SecureSharedPreferences.Encrypter] and
     * [SecureSharedPreferences.Decrypter]
     */
    class CryptoKeyGenerator(private val sekrit: CharArray) {
        operator fun invoke(): Key {
            val digester = MessageDigest.getInstance(DIGESTER_ALGORITHM)
            digester.update(String(sekrit).toByteArray())
            val key = digester.digest()
            return SecretKeySpec(key, PREFERENCES_ENCRYPT_ALGORITHM)
        }
    }

    /** Generates a sekrit [CharArray] */
    class SekritGenerator(
        private val symmetricKeyPreferences: SharedPreferences,
        private val encrypter: AsymmetricKeyEncrypter,
        private val decrypter: AsymmetricKeyDecrypter
    ) {
        operator fun invoke(): CharArray {
            val key = decrypter(symmetricKeyPreferences[PREF_KEY])
            val sekrit = if (key.isNullOrBlank()) {
                // Store a new random UUID if none is found
                val newKey = UUID.randomUUID().toString()
                symmetricKeyPreferences[PREF_KEY] = encrypter(newKey)
                newKey
            } else {
                key
            }
            return sekrit.toCharArray()
        }

        companion object {
            const val PREF_KEY = "SEKRIT"
        }
    }

    /** Encrypter for asymmetric key */
    class AsymmetricKeyEncrypter(private val cipher: Cipher) {
        /**
         * @return [String]
         * TODO: May throw exception?
         */
        operator fun invoke(value: String): String {
            val bytes = value.toByteArray()
            return cipher.doFinal(bytes).encodeBase64String(Base64.NO_WRAP)
        }
    }

    /** Decrypter for asymmetric key */
    class AsymmetricKeyDecrypter(private val cipher: Cipher) {

        /** @return [String] or `null` if error or [value] is `null` */
        operator fun invoke(value: String?): String? {
            val encryptedData = value?.decodeBase64(Base64.NO_WRAP) ?: return null
            return try {
                String(cipher.doFinal(encryptedData))
            } catch (e: BadPaddingException) {
                Timber.e(e, "Cannot decrypt: '$value'")
                null
            }
        }
    }

    /**
     * A provider for [KeyPair]
     * @param context is required for create [KeyPairGeneratorSpec] before [Android.MARSHMALLOW] and
     * for switch the current [Locale] as a workaround for RTL languages
     */
    class KeyPairProvider(
        private val context: Context,
        private val keyPairGenerator: KeyPairGenerator = DefaultKeyPairGenerator,
        private val keyStore: KeyStore = DefaultKeyStore
    ) {

        /**
         * @return [KeyPair]
         * Try to get from [keyStore] or generate a new one if fails
         */
        operator fun invoke(alias: String): KeyPair =
            runCatching { retrieveAsymmetricKeyPair(alias) }.getOrNull() ?: generateKeyPair(alias)

        /** @throws UnrecoverableKeyException */
        private fun retrieveAsymmetricKeyPair(alias: String): KeyPair {
            return KeyPair(
                keyStore.getCertificate(alias)?.publicKey,
                keyStore.getKey(alias, null) as PrivateKey
            )
        }

        private fun generateKeyPair(alias: String): KeyPair {
            // workaround for BouncyCastle crashing when parsing Date in RTL languages
            // we set locale temporarily to US and then go back
            val defaultLocale = Locale.getDefault()
            setLocale(Locale.US)

            val start = GregorianCalendar()
            val end = GregorianCalendar().apply { add(Calendar.YEAR, 5) }

            // The KeyPairGeneratorSpec object is how parameters for your key pair are passed
            // to the KeyPairGenerator.
            val algorithmParameterSpec = if (Android.MARSHMALLOW) {
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setCertificateSubject(X500Principal("CN=ProtonMail, O=Android Authority"))
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateNotBefore(start.time)
                    .setCertificateNotAfter(end.time)
                    .build()
            } else {
                @Suppress("DEPRECATION") // Old APIs
                KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=ProtonMail, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
            }

            keyPairGenerator.initialize(algorithmParameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()

            setLocale(defaultLocale)

            return keyPair
        }

        private fun setLocale(locale: Locale) {
            Locale.setDefault(locale)
            val resources = context.resources
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        }
    }
    // endregion

    // region Extensions
    private fun Any.encrypt() = encrypter(this.toString())
    private fun String.decrypt() = decrypter(this)
    private inline fun <reified T: Any> String.decryptAs() = decryptAs(T::class)
    private fun <T: Any> String.decryptAs(kClass: KClass<T>) : T? {
        val decryptedString = decrypt() ?: return null
        @Suppress("UNCHECKED_CAST")
        return when(PrefType.get(kClass)) {
            BOOLEAN ->  decryptedString.toBooleanOrNull()
            FLOAT ->    decryptedString.toFloatOrNull()
            INT ->      decryptedString.toIntOrNull()
            LONG ->     decryptedString.toLongOrNull()
            STRING ->   decryptedString
        } as T?
    }
    // endregion
}

// region Const
private const val ASYMMETRIC_ENCRYPTION_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
private const val ASYMMETRIC_KEY_ALIAS = "ProtonKey"
private const val DIGESTER_ALGORITHM = SHA_256
private const val KEY_STORE_NAME = "AndroidKeyStore"
private const val KEY_PAIR_ALGORITHM = RSA
private const val PREFERENCES_ENCRYPT_ALGORITHM = AES
// endregion
// region Default params
private val DefaultKeyStore = KeyStore.getInstance(KEY_STORE_NAME).apply { load(null) }
private val DefaultKeyPairGenerator =
    KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM, KEY_STORE_NAME)
// endregion
