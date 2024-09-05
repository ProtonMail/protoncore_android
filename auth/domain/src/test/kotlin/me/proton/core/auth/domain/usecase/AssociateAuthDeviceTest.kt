package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceTokenString
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssociateAuthDeviceTest {
    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var authDeviceRepository: AuthDeviceRepository

    @MockK
    private lateinit var checkOtherDevices: CheckOtherDevices

    @MockK
    private lateinit var cryptoContext: CryptoContext

    @MockK
    private lateinit var deviceSecretRepository: DeviceSecretRepository

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    private lateinit var tested: AssociateAuthDevice

    private val testDeviceId = AuthDeviceId("device-id")
    private val testEncryptedDeviceToken: DeviceTokenString = "encrypted-device-token"
    private val testDecryptedDeviceToken: String = "device-token"
    private val testEncryptedSecret = "encrypted-secret"
    private val testSessionId: SessionId = SessionId("session-id")
    private val testUserId: UserId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto
        every { keyStoreCrypto.decrypt(testEncryptedDeviceToken) } returns testDecryptedDeviceToken
        tested = AssociateAuthDevice(
            accountRepository,
            authDeviceRepository,
            checkOtherDevices,
            cryptoContext,
            deviceSecretRepository,
        )
    }

    @Test
    fun `device successfully associated`() = runTest {
        // GIVEN
        coEvery { accountRepository.getSessionIdOrNull(testUserId) } returns testSessionId
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } returns testEncryptedSecret

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            userId = testUserId
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Success(testEncryptedSecret), result)
    }

    @Test
    fun `server error`() = runTest {
        // GIVEN
        val exception = ApiException(ApiResult.Error.Http(500, "Server error"))
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                testDeviceId,
                testEncryptedDeviceToken,
                hasTemporaryPassword = true,
                sessionId = testSessionId,
                userId = testUserId
            )
        }

        // THEN
        assertEquals(exception, actual)
    }

    @Test
    fun `device already associated`() = runTest {
        // GIVEN
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Device already associated")
            )
        )

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            sessionId = testSessionId,
            userId = testUserId
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.SessionAlreadyAssociated, result)
    }

    @Test
    fun `bad request`() = runTest {
        // GIVEN
        val exception = ApiException(ApiResult.Error.Http(400, "Bad request"))
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                testDeviceId,
                testEncryptedDeviceToken,
                hasTemporaryPassword = true,
                sessionId = testSessionId,
                userId = testUserId
            )
        }

        // THEN
        assertEquals(exception, actual)
    }

    @Test
    fun `unprocessable request`() = runTest {
        // GIVEN
        val exception = ApiException(ApiResult.Error.Http(422, "Unprocessable"))
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                testDeviceId,
                testEncryptedDeviceToken,
                hasTemporaryPassword = true,
                sessionId = testSessionId,
                userId = testUserId
            )
        }

        // THEN
        assertEquals(exception, actual)
    }

    @Test
    fun `invalid token`() = runTest {
        // GIVEN
        coJustRun { deviceSecretRepository.deleteAll(testUserId) }
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                422,
                "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.AUTH_DEVICE_TOKEN_INVALID, "Invalid token")
            )
        )

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            sessionId = testSessionId,
            userId = testUserId
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceTokenInvalid, result)
        coVerify { deviceSecretRepository.deleteAll(testUserId) }
    }

    @Test
    fun `device not found`() = runTest {
        // GIVEN
        coJustRun { authDeviceRepository.deleteById(testDeviceId, testUserId) }
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                422,
                "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.AUTH_DEVICE_NOT_FOUND, "Device not found")
            )
        )

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            sessionId = testSessionId,
            userId = testUserId
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceNotFound, result)
        coVerify { authDeviceRepository.deleteById(testDeviceId, testUserId) }
    }

    @Test
    fun `device not active`() = runTest {
        // GIVEN
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                422,
                "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.AUTH_DEVICE_NOT_ACTIVE, "Device not active")
            )
        )
        coEvery {
            checkOtherDevices(
                any(),
                testUserId
            )
        } returns CheckOtherDevices.Result.LoginWithBackupPasswordAvailable

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            sessionId = testSessionId,
            userId = testUserId
        )

        // THEN
        assertEquals(
            AssociateAuthDevice.Result.Error.DeviceNotActive(CheckOtherDevices.Result.LoginWithBackupPasswordAvailable),
            result
        )
    }

    @Test
    fun `device rejected`() = runTest {
        // GIVEN
        coJustRun { authDeviceRepository.deleteById(testDeviceId, testUserId) }
        coEvery {
            authDeviceRepository.associateDeviceWithSession(
                testSessionId,
                testDeviceId,
                testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                422,
                "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.AUTH_DEVICE_REJECTED, "Device rejected")
            )
        )

        // WHEN
        val result = tested(
            testDeviceId,
            testEncryptedDeviceToken,
            hasTemporaryPassword = true,
            sessionId = testSessionId,
            userId = testUserId
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceRejected, result)
        coVerify { authDeviceRepository.deleteById(testDeviceId, testUserId) }
    }
}
