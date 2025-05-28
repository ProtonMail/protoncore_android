package me.proton.core.user.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserLocalDataSource
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRemoteDataSourceImplTest {
    @MockK(relaxed = true)
    private lateinit var apiManagerFactory: ApiManagerFactory

    @MockK(relaxed = true)
    private lateinit var sessionProvider: SessionProvider

    @MockK(relaxed = true)
    private lateinit var userLocalDataSource: UserLocalDataSource

    @MockK
    private lateinit var userApi: UserApi

    private lateinit var apiProvider: ApiProvider
    private val dispatcherProvider = TestDispatcherProvider(UnconfinedTestDispatcher())

    private lateinit var tested: UserRemoteDataSourceImpl
    private val testUserId = UserId("test-user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)

        val apiManager = object : ApiManager<UserApi> {
            override suspend fun <T> invoke(
                forceNoRetryOnConnectionErrors: Boolean,
                block: suspend UserApi.() -> T
            ): ApiResult<T> = ApiResult.Success(block.invoke(userApi))
        }
        every { apiManagerFactory.create(any(), interfaceClass = UserApi::class) } returns apiManager

        tested = UserRemoteDataSourceImpl(apiProvider, userLocalDataSource)
    }

    @Test
    fun `fetch user`() = runTest {
        // GIVEN
        coEvery { userLocalDataSource.getCredentialLessUser(testUserId) } returns null

        val usersResponse = UsersResponse(mockk(relaxed = true))
        coEvery { userApi.getUsers() } returns usersResponse

        // WHEN
        val result = tested.fetch(testUserId)

        // THEN
        assertEquals(usersResponse.user.toUser(), result)
    }

    @Test
    fun `do not fetch if credential-less`() = runTest {
        // GIVEN
        val credentialLessUser = mockk<User> {
            every { type } returns Type.CredentialLess
        }
        coEvery { userLocalDataSource.getCredentialLessUser(testUserId) } returns credentialLessUser

        // WHEN
        val result = tested.fetch(testUserId)

        // THEN
        assertEquals(credentialLessUser, result)
        coVerify(exactly = 0) { userApi.getUsers() }
    }
}