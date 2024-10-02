package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.DeviceTokenString
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.auth.domain.usecase.sso.AssociateAuthDevice
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssociateAuthDeviceTest {

    @MockK
    private lateinit var authDeviceRepository: AuthDeviceRepository

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
    private val testUserId: UserId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto
        every { keyStoreCrypto.decrypt(testEncryptedDeviceToken) } returns testDecryptedDeviceToken
        tested = AssociateAuthDevice(
            cryptoContext,
            authDeviceRepository,
            deviceSecretRepository,
        )
    }

    @Test
    fun `device successfully associated`() = runTest {
        // GIVEN
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
            )
        } returns testEncryptedSecret

        // WHEN
        val result = tested(
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Success(testEncryptedSecret), result)
    }

    @Test
    fun `server error`() = runTest {
        // GIVEN
        val exception = ApiException(ApiResult.Error.Http(500, "Server error"))
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testEncryptedDeviceToken
            )
        }

        // THEN
        assertEquals(exception, actual)
    }

    @Test
    fun `device already associated`() = runTest {
        // GIVEN
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
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
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.SessionAlreadyAssociated, result)
    }

    @Test
    fun `bad request`() = runTest {
        // GIVEN
        val exception = ApiException(ApiResult.Error.Http(400, "Bad request"))
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testEncryptedDeviceToken
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
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
            )
        } throws exception

        // WHEN
        val actual = assertFailsWith<ApiException> {
            tested(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testEncryptedDeviceToken
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
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
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
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceTokenInvalid, result)
        coVerify { deviceSecretRepository.deleteAll(testUserId) }
    }

    @Test
    fun `device not found`() = runTest {
        // GIVEN
        coJustRun { authDeviceRepository.deleteByDeviceId(testUserId, testDeviceId) }
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
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
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceNotFound, result)
        coVerify { authDeviceRepository.deleteByDeviceId(testUserId, testDeviceId) }
    }

    @Test
    fun `device not active`() = runTest {
        // GIVEN
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
            )
        } throws ApiException(
            ApiResult.Error.Http(
                422,
                "Unprocessable",
                ApiResult.Error.ProtonData(ResponseCodes.AUTH_DEVICE_NOT_ACTIVE, "Device not active")
            )
        )

        // WHEN
        val result = tested(
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(
            AssociateAuthDevice.Result.Error.DeviceNotActive,
            result
        )
    }

    @Test
    fun `device rejected`() = runTest {
        // GIVEN
        coJustRun { authDeviceRepository.deleteByDeviceId(testUserId, testDeviceId) }
        coEvery {
            authDeviceRepository.associateDevice(
                userId = testUserId,
                deviceId = testDeviceId,
                deviceToken = testDecryptedDeviceToken
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
            userId = testUserId,
            deviceId = testDeviceId,
            deviceToken = testEncryptedDeviceToken
        )

        // THEN
        assertEquals(AssociateAuthDevice.Result.Error.DeviceRejected, result)
        coVerify { authDeviceRepository.deleteByDeviceId(testUserId, testDeviceId) }
    }
}
