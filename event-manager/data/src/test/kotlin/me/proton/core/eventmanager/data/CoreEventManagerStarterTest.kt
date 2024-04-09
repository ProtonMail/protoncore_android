package me.proton.core.eventmanager.data

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.IsCoreEventManagerEnabled
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.test.android.ArchTest
import me.proton.core.test.android.lifecycle.TestLifecycle
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreEventManagerStarterTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var appLifecycleProvider: AppLifecycleProvider

    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var eventManagerProvider: EventManagerProvider

    @MockK
    private lateinit var isCoreEventManagerEnabled: IsCoreEventManagerEnabled

    @MockK(relaxed = true)
    private lateinit var eventMetadataRepository: EventMetadataRepository

    private lateinit var tested: CoreEventManagerStarter

    private val testUserId = UserId("user-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = CoreEventManagerStarter(
            appLifecycleProvider,
            accountManager,
            eventManagerProvider,
            isCoreEventManagerEnabled,
            eventMetadataRepository
        )
    }

    @Test
    fun `event loop is started`() = runTest {
        // GIVEN
        val account = mockReadyAccount()
        val accountFlow = MutableStateFlow(account)
        val eventManager = mockk<EventManager>(relaxed = true)
        val lifecycleOwner = TestLifecycle()

        every { accountManager.onAccountStateChanged(any()) } returns accountFlow
        every { appLifecycleProvider.lifecycle } returns lifecycleOwner.lifecycle
        coEvery { eventManagerProvider.get(any()) } returns eventManager
        every { isCoreEventManagerEnabled(any()) } returns true

        // WHEN
        tested.start()
        lifecycleOwner.create()
        testScheduler.advanceUntilIdle()

        // THEN
        coVerify { eventManager.start() }
    }

    @Test
    fun `event loop is stopped`() = runTest {
        // GIVEN
        val account = mockReadyAccount()
        val accountFlow = MutableStateFlow(account)
        val eventManager = mockk<EventManager>(relaxed = true)
        val lifecycleOwner = TestLifecycle()

        every { accountManager.onAccountStateChanged(any()) } returns accountFlow
        every { appLifecycleProvider.lifecycle } returns lifecycleOwner.lifecycle
        coEvery { eventManagerProvider.get(any()) } returns eventManager
        every { isCoreEventManagerEnabled(any()) } returns false

        // WHEN
        tested.start()
        lifecycleOwner.create()
        testScheduler.advanceUntilIdle()

        // THEN
        coVerify { eventManager.stop() }
    }

    @Test
    fun `initial eventId is set`() = runTest {
        // GIVEN
        val testEventId = EventId("event-id")
        val account = mockReadyAccount(sessionDetails = mockk {
            every { initialEventId } returns testEventId.id
        })
        val accountFlow = MutableStateFlow(account)
        val eventManager = mockk<EventManager>(relaxed = true)
        val lifecycleOwner = TestLifecycle()

        every { accountManager.onAccountStateChanged(any()) } returns accountFlow
        every { appLifecycleProvider.lifecycle } returns lifecycleOwner.lifecycle
        coEvery { eventManagerProvider.get(any()) } returns eventManager
        every { isCoreEventManagerEnabled(any()) } returns true

        // WHEN
        tested.start()
        lifecycleOwner.create()
        testScheduler.advanceUntilIdle()

        // THEN
        coVerify { eventManager.start() }

        val configSlot = slot<EventManagerConfig>()
        val eventIdSlot = slot<EventId>()
        coVerify { eventMetadataRepository.setInitialEventId(capture(configSlot), capture(eventIdSlot)) }
        assertEquals(EventManagerConfig.Core(testUserId), configSlot.captured)
        assertEquals(testEventId, eventIdSlot.captured)
    }

    private fun mockReadyAccount(sessionDetails: SessionDetails? = null): Account = mockk<Account> {
        every { details } returns mockk {
            every { session } returns sessionDetails
        }
        every { state } returns AccountState.Ready
        every { userId } returns testUserId
    }
}
