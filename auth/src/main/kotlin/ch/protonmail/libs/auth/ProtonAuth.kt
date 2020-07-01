package ch.protonmail.libs.auth

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import ch.protonmail.libs.auth.AuthResult.Failure.NoNetwork
import ch.protonmail.libs.auth.api.*
import ch.protonmail.libs.auth.model.response.*
import ch.protonmail.libs.core.connection.NetworkManager
import ch.protonmail.libs.crypto.OpenPGP
import java.io.UnsupportedEncodingException

/**
 * This class will contain high level instruction for handling auth across Proton applications.
 * The declared services are based on Retrofit, we can always create some clean interfaces in may
 * we will require another framework for network calls.
 *
 * @author Davide Farella
 */
internal class ProtonAuth(
    private val networkManager: NetworkManager,
    private val tokenManagerFactory: TokenManager.MultiUserProvider,
    private val openPGP: OpenPGP,
    private val addressSpec: AddressSpec,
    private val authSpec: AuthenticationSpec,
    private val keySpec: KeySpec,
    private val mailSettingsSpec: MailSettingsSpec,
    private val userSpec: UserSpec,
    private val userSettingsService: UserSettingsService
) {

    /** @return new [TokenManager] */
    private val tokenManager get() = tokenManagerFactory()

    init {
        // Assert that clientSecret has been initialized
        try {
            ProtonAuthConfig.clientSecret
        } catch (e: UninitializedPropertyAccessException) {
            throw IllegalStateException(
                "`ProtonAuthConfig.clientSecret` has not been initialized," +
                        "it should be set on the Application's creation. See `Application.onCreate()`"
            )
        }
    }
//
//    /** @return [AuthResult] of [UserInfo] */
//    suspend fun createUser(
//        username: String,
//        password: String,
//        updateMe: Boolean,
//        tokenType: String,
//        token: String
//    ) = doWithNetwork {
//        // TODO: handle payment token type
//        val modulus = authSpec.randomModulus()
//        val userInfo = userSpec.createUser(
//            username,
//            PasswordVerifier.calculate(password, modulus),
//            updateMe,
//            tokenType,
//            token
//        )
//        Success.CreateUser(userInfo)
//    }
//
//    /** @return [AuthResult] of [GenerateKeyResponse] */
//    suspend fun generateKeys(
//        username: String,
//        password: String,
//        bits: Int,
//        domain: String = PROTONMAIL_COM
//    ) : AuthResult<GenerateKeyResponse> {
//        return try {
//            val salt = ByteArray(16)
//            SecureRandom().nextBytes(salt)
//            val stringSalt = salt.encodeBase64String()
//
//            val keySalt = stringSalt[0, stringSalt.length - 1]
//            val generatedMailboxPassword = generateMailboxPassword(password, keySalt)
//
//            val privateKey = openPGP.generateKey(username, domain, generatedMailboxPassword, KeyType.RSA, bits)
//            Success.GenerateKey(GenerateKeyResponse(keySalt, privateKey, generatedMailboxPassword))
//
//        } catch (t: Throwable) {
//            Failure.Unknown(t)
//        }
//    }
//
//    suspend fun mailboxLogin(username: String, password: String, keySalt: String, signUp: Boolean) = doWithNetwork {
//        Success.MailboxLogin(TODO("LoginResponse or MailboxLoginResponse"))
//    }
//
//    /** @return [AuthResult] of [LoginResponse] */
//    suspend fun login(/* TODO params */) = doWithNetwork {
//        Success.Login(TODO("LoginResponse"))
//    }
//
//    private suspend fun handleLogin(
//        username: String,
//        password: String,
//        twoFactor: String,
//        rememberMe: Boolean,
//        infoResponse: LoginInfoResponse,
//        fallbackAuthVersion: Int,
//        signUp: Boolean
//    ) = doWithNetwork {
//
//        // FIXME
//        var redirectToSetup = false
//
//        try {
//            val proofs = srpProofsForInfo(username, password, infoResponse, fallbackAuthVersion)
//            if (proofs != null) {
//                val loginResponse = runCatching {
//                    authSpec.login(
//                        username,
//                        infoResponse.srpSession,
//                        proofs.clientEphemeral,
//                        proofs.clientProof,
//                        twoFactor
//                    )
//                }.getOrNull()
//
//                val isProofOk = loginResponse?.isValid == true && ConstantTime.isEqual(
//                    proofs.expectedServerProof,
//                    loginResponse.serverProof.decodeBase64()
//                )
//
//                if (isProofOk) {
//                    // If proof is ok, loginResponse is not `null`
//                    loginResponse!!
//                    val keySalt = loginResponse.keySalt
//
//                    // FIXME
//                    //  userManager.setUsernameAndReloadTokenManager(username)
//                    //  tokenManager = userManager.getTokenManager(username)
//                    tokenManagerFactory.username = username
//                    tokenManager.handleLogin(loginResponse)
//
//                    if (keySalt == null) { // new response doesn't contain salt
//                        val userInfo = userSpec.fetchUserInfo()
//                        val keySalts = userSpec.fetchKeySalts()
//
//                        val primaryKey = userInfo.user.keys.find { it.primary }
//                            ?: return@doWithNetwork Failure.Unknown()
//                        tokenManager.encryptedPrivateKey = primaryKey.privateKey // it's needed for verification later
//
//                        keySalts.keySalts.find { it.id == primaryKey.id }
//                            ?: return@doWithNetwork Failure.Unknown()
//
//                        if (userInfo.user.keys.isEmpty()) {
//                            redirectToSetup = true
//                        }
//                    } else { // old response with key and salt
//
//                        if (loginResponse.privateKey.isEmpty()) {
//                            redirectToSetup = true
//                        }
//                    }
//
//                    if (infoResponse.authVersion < PasswordUtils.LAST_AUTH_VERSION) {
//                        val modulus = authSpec.randomModulus()
//                        val generatedMailboxPassword = try {
//                            generateMailboxPassword(password, keySalt)
//                        } catch (e: UnsupportedEncodingException) {
//                            Timber.e(e)
//                            null
//                        }
//
//                        tokenManager.decryptAccessToken(generatedMailboxPassword)
//
//                        userSettingsSpec.upgradeLoginPassword(
//                            UpgradePasswordBody(PasswordVerifier.calculate(password, modulus))
//                        )
//
//                        tokenManager.clearAccessToken()
//
//                    }
//                    if (redirectToSetup && !signUp) {
//                        val userInfo = userSpec.fetchUserInfo()
//                        val userSettingsResp = userSettingsSpec.fetchUserSettings()
//                        val mailSettingsResp = mailSettingsSpec.fetchMailSettings()
//                        val user = userInfo.user
//
//                        // FIXME
//                        //  userManager.setUserSettings(userSettingsResp.userSettings)
//                        //  userManager.setMailSettings(mailSettingsResp.mailSettings)
//                        //  domainName = getDomainName(user)
//
//                    }
//                    if (signUp || loginResponse.passwordMode == PasswordMode.Single && !keySalt.isNullOrBlank()) {
//                        if (signUp) {
//                            // FIXME
//                            //  if (TextUtils.isEmpty(tokenManager.getEncPrivateKey())) {
//                            //      tokenManager.setEncPrivateKey(userManager.getPrivateKey())
//                            //  }
//                            mailboxLogin(
//                                username,
//                                password,
//                                keySalt!! /* FIXME ?: userManager.getKeySalt() */,
//                                true
//                            )
//                        } else {
//                            mailboxLogin(username, password, keySalt!!, false)
//                        }
//                        return
//                    }
//                } else if (loginResponse?.isValid != true) {
//                    Failure.InvalidCredentials
//                } else if (!ConstantTime.isEqual(proofs.expectedServerProof, loginResponse.serverProof.decodeBase64())) {
//                    Failure.InvalidServerProof
//                }
//            }
//        } catch (e: Exception) {
//            Failure.Unknown(e)
//        }
//
//        if (infoResponse == null || status === AuthStatus.FAILED) {
//            userManager.logoutOffline()
//            AppUtil.postEventOnUi(LoginEvent(AuthStatus.FAILED, null, false, null, null))
//            return
//        }
//        if (infoResponse.authVersion == 0 && status.equals(AuthStatus.INVALID_CREDENTIAL) && fallbackAuthVersion != 0) {
//            val newFallback = if (fallbackAuthVersion == 2 && PasswordUtils.cleanUserName(username) != username.toLowerCase()) {
//                1
//            } else {
//                0
//            }
//
//            startInfo(username, password, rememberMe, newFallback)
//        } else {
//            AppUtil.postEventOnUi(LoginEvent(status, keySalt, redirectToSetup, user, domainName))
//        }
//    }
//
//    /** @return [AuthResult] of [LoginInfoResponse] */
//    suspend fun loginInfo(username: String) = doWithNetwork {
//        try {
//            val response = authSpec.loginInfoForAuthentication(username)
//            Success.LoginInfo(response)
//
//        } catch (t: Throwable) {
//            Failure.Unknown(t)
//        }
//    }
//
//    /** @return [AuthResult] of [AddressSetupResponse] */
//    suspend fun setupAddress(domain: String) = doWithNetwork {
//        try {
//            val response = addressSpec.setupAddress(AddressSetupBody(domain))
//            Success.SetupAddress(response)
//
//        } catch (t: Throwable) {
//            Failure.Unknown(t)
//        }
//    }
//
//    /** @return [AuthResult] of [UserInfo] */
//    suspend fun setupKeys(
//        addressId: String,
//        password: String,
//        mailboxPassword: String,
//        keySalt: String,
//        privateKey: String
//    ) = doWithNetwork {
//        val newModulus = authSpec.randomModulus()
//        val verifier = PasswordVerifier.calculate(password, newModulus)
//
//        val signedKeyList = generateSignedKeyList(privateKey, mailboxPassword)
//        val addressKey = AddressKey(addressId, privateKey, signedKeyList)
//        val addressKeys = listOf(addressKey)
//        val keysSetupBody = KeysSetupBody(privateKey, keySalt, addressKeys, verifier)
//
//        try {
//            val response = keySpec.setupKeys(keysSetupBody)
//            Success.SetupKeys(response)
//
//        } catch (t: Throwable) {
//            Failure.Unknown(t)
//        }
//
//        // FIXME
//        //  val userInfo = api.fetchUserInfo()
//        //  val userSettings = api.fetchUserSettings()
//        //  val mailSettings = api.fetchMailSettings()
//        //  val addressesResponse = api.fetchAddresses()
//        //  val user = userInfo.getUser()
//        //  userManager.setUserSettings(userSettings.getUserSettings())
//        //  userManager.setMailSettings(mailSettings.getMailSettings())
//        //  user.setAddresses(addressesResponse.getAddresses())
//        //  userManager.setUser(user)
//    }
//
//    // region private methods
//    private fun generateMailboxPassword(password: String, keySalt: String?): String {
//        if (keySalt.isNullOrBlank()) return password
//
//        val encodedSalt: String = ConstantTime
//            .encodeBase64DotSlash(keySalt.decodeBase64() + "proton".toByteArray(Charsets.UTF_8), false)
//
//        val hashedPassword = BCrypt.hashpw(password, BCRYPT_PREFIX + encodedSalt)
//        val passIndex = hashedPassword.length - 31
//
//        return hashedPassword.substring(passIndex)
//    }
//
//    private fun generateSignedKeyList(key: String, mailboxPassword: String): SignedKeyList {
//        val keyFingerprint = openPGP.getFingerprint(key)
//        val keyList = """[{"Fingerprint": "$keyFingerprint", "Primary": 1, "Flags": 3}]"""
//        val signedKeyList = openPGP.signTextDetached(keyList, key, mailboxPassword, true)
//        return SignedKeyList(keyList, signedKeyList)
//    }
//
//    /**
//     * @return [SrpClient.Proofs]
//     * @throws NoSuchAlgorithmException
//     */
//    private fun srpProofsForInfo(
//        username: String,
//        password: String,
//        infoResponse: LoginInfoResponse,
//        fallbackAuthVersion: Int
//    ): SrpClient.Proofs? {
//        var authVersion = infoResponse.authVersion
//        if (authVersion == 0) {
//            authVersion = fallbackAuthVersion
//        }
//
//        if (authVersion <= 2) {
//            return null
//        }
//
//        val modulus = Armor.readClearSignedMessage(infoResponse.modulus).decodeBase64()
//
//        val hashedPassword = PasswordUtils.hashPassword(
//            authVersion,
//            password,
//            username,
//            infoResponse.salt.decodeBase64(),
//            modulus
//        )
//
//        return SrpClient.generateProofs(
//            2048,
//            modulus,
//            infoResponse.serverEphemeral.decodeBase64(),
//            hashedPassword
//        )
//    }
//
    /**
     * Execute [block] and return its result if Network is available, else return [NoNetwork]
     * @return [AuthResult]
     */
    @VisibleForTesting(otherwise = PRIVATE)
    internal suspend inline fun <T : AuthResult<R>, R> doWithNetwork(crossinline block: suspend () -> T): T {
        @Suppress("UNCHECKED_CAST") // `NoNetwork` is `AuthResult`
        return if (networkManager.canUseNetwork()) block()
        else NoNetwork as T
    }
    // endregion
}

/** Result of Auth operation. It could be a [AuthResult.Success] or a [AuthResult.Failure] */
internal sealed class AuthResult<out R : Any?> {

    /** @return [R] response received for Auth operation if this is [Success] else `null` */
    open val response: R? get() = if (this is Success) this.response else null

    /** Auth operation has succeed */
    sealed class Success<out R : Any> : AuthResult<R>() {

        /** Override of [AuthResult.response] for drop the nullability for [Success] results */
        abstract override val response: R

        /** Response for [ProtonAuth.createUser] */
        class CreateUser(override val response: UserInfo) : Success<UserInfo>()

        /** Response for [ProtonAuth.generateKeys] */
        class GenerateKey(override val response: GenerateKeyResponse) : Success<GenerateKeyResponse>()

        /** Response for [ProtonAuth.login] */
        class Login(override val response: LoginResponse) : Success<LoginResponse>()

        /** Response for [ProtonAuth.loginInfo] */
        class LoginInfo(override val response: LoginInfoResponse) : Success<LoginInfoResponse>()

        /** Response for [ProtonAuth.mailboxLogin] */
        class MailboxLogin(override val response: LoginResponse) : Success<LoginResponse>()

        /** Response for [ProtonAuth.setupAddress] */
        class SetupAddress(override val response: AddressSetupResponse) : Success<AddressSetupResponse>()

        /** Response for [ProtonAuth.setupKeys] */
        class SetupKeys(override val response: UserInfo) : Success<UserInfo>()
    }

    /** Auth operation has failed */
    sealed class Failure : AuthResult<Nothing>() {

        /** Optional [Throwable] describing the failure */
        open val throwable: Throwable? = null

        /** Credentials are not valid */
        object InvalidCredentials : Failure()

        /** Server proof is not valid */
        object InvalidServerProof : Failure()

        /** Network is not available */
        object NoNetwork : Failure()

        /** Failure relative to [UnsupportedEncodingException] */
        class UnsupportedEncoding(override val throwable: UnsupportedEncodingException) : Failure()

        /** Unknown error */
        class Unknown(override val throwable: Throwable? = null) : Failure()
    }
}
