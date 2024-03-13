package me.proton.core.accountrecovery.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import kotlin.test.BeforeTest
import kotlin.test.Test

class StartRecoveryTest {

    private val testUserId = UserId("user-id")
    private val testConfig = EventManagerConfig.Core(testUserId)

    private var accountRecoveryRepository = mockk<AccountRecoveryRepository> {
        coJustRun { this@mockk.startRecovery(any()) }
    }

    private val eventManager = mockk<EventManager> {
        coEvery { this@mockk.suspend<Unit>(captureLambda()) } coAnswers {
            lambda<(suspend () -> Unit)>().captured()
        }
    }

    private val eventManagerProvider = mockk<EventManagerProvider> {
        coEvery { this@mockk.get(testConfig) } returns eventManager
    }

    private lateinit var tested: StartRecovery

    @BeforeTest
    fun setUp() {
        tested = StartRecovery(
            accountRecoveryRepository = accountRecoveryRepository,
            eventManagerProvider = eventManagerProvider
        )
    }

    @Test
    fun startRecovery() = runTest {
        // WHEN
        tested(testUserId)

        // THEN
        coVerify { accountRecoveryRepository.startRecovery(testUserId) }
        coVerify { eventManager.suspend(any()) }
    }
}
