package me.proton.core.user.data

import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class UserSpaceEventListenerTest {
    @MockK
    private lateinit var db: UserDatabase

    @MockK(relaxed = true)
    private lateinit var userRepository: UserRepository

    private val testUserId = UserId("test_user_id")
    private val testConfig = EventManagerConfig.Core(testUserId)
    private lateinit var tested: UserSpaceEventListener

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = UserSpaceEventListener(db, userRepository)
    }

    @Test
    fun deserializeEmpty() = runTest {
        val response = EventsResponse("{}")
        val events = tested.deserializeEvents(testConfig, response)
        assertContentEquals(
            listOf(Event(Action.Update, testUserId.id, UserSpaceEvent())),
            events
        )
    }

    @Test
    fun deserializeNulls() = runTest {
        val response = EventsResponse(
            """{ "UsedSpace": null, "UsedBaseSpace": null, "UsedDriveSpace": null }"""
        )
        val events = tested.deserializeEvents(testConfig, response)
        assertContentEquals(
            listOf(Event(Action.Update, testUserId.id, UserSpaceEvent())),
            events
        )
    }

    @Test
    fun deserializeNonNullFields() = runTest {
        val response = EventsResponse(
            """{ "UsedSpace": 10, "UsedBaseSpace": 3, "UsedDriveSpace": 7 }"""
        )
        val events = tested.deserializeEvents(testConfig, response)
        assertContentEquals(
            listOf(
                Event(
                    Action.Update,
                    testUserId.id,
                    UserSpaceEvent(usedSpace = 10, usedBaseSpace = 3, usedDriveSpace = 7)
                )
            ),
            events
        )
    }

    @Test
    fun updateNulls() = runTest {
        tested.onUpdate(testConfig, listOf(UserSpaceEvent()))
        coVerify { userRepository wasNot Called }
    }

    @Test
    fun updateNonNullValues() = runTest {
        tested.onUpdate(
            testConfig,
            listOf(UserSpaceEvent(usedSpace = 10, usedBaseSpace = 3, usedDriveSpace = 7))
        )
        coVerify { userRepository.updateUserUsedSpace(testUserId, 10) }
        coVerify { userRepository.updateUserUsedBaseSpace(testUserId, 3) }
        coVerify { userRepository.updateUserUsedDriveSpace(testUserId, 7) }
    }
}