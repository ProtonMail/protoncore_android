package me.proton.core.auth.data.event

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.data.api.response.MemberDeviceResource
import me.proton.core.auth.data.db.AuthDatabase
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.repository.MemberDeviceLocalDataSource
import me.proton.core.auth.domain.repository.MemberDeviceRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemberDeviceEventListenerTest {
    @MockK
    private lateinit var authDatabase: AuthDatabase

    @MockK
    private lateinit var memberDeviceLocalDataSource: MemberDeviceLocalDataSource

    @MockK
    private lateinit var memberDeviceRepository: MemberDeviceRepository

    private lateinit var tested: MemberDeviceEventListener

    private val testUserId = UserId("test-user-id")
    private val testConfig = EventManagerConfig.Core(testUserId)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = MemberDeviceEventListener(authDatabase, memberDeviceLocalDataSource, memberDeviceRepository)
    }

    @Test
    fun `no events`() = runTest {
        val response = EventsResponse("""{ "More": 0 }""")
        assertNull(tested.deserializeEvents(testConfig, response))
    }

    @Test
    fun `empty events`() = runTest {
        val response = EventsResponse("""{ "MemberAuthDevices": [] }""")
        assertEquals(true, tested.deserializeEvents(testConfig, response)?.isEmpty())
    }

    @Test
    fun `null events`() = runTest {
        val response = EventsResponse("""{ "MemberAuthDevices": null }""")
        assertNull(tested.deserializeEvents(testConfig, response))
    }

    @Test
    fun `member device event`() = runTest {
        val response = EventsResponse(
            """
            { "MemberAuthDevices": [
                {
                    "ID": "device-1",
                    "Action": 1,
                    "MemberAuthDevice": {
                        "ID": "device-1",
                        "MemberID": "member-1",
                        "ActivationAddressID": "address-1",
                        "State": 1,
                        "Name": "Mobile Phone 1",
                        "LocalizedClientName": "Proton Account for Web",
                        "Platform": "Web",
                        "CreateTime": 1715720090,
                        "ActivateTime": 1715720090,
                        "RejectTime": 1715720090,
                        "ActivationToken": "activation-token",
                        "LastActivityTime": 1715720090
                    }
                }
            ]}
            """.trimIndent()
        )

        val events = tested.deserializeEvents(testConfig, response)
        assertEquals(1, events?.size)
        assertEquals(
            Event(
                Action.Create, "device-1", MemberDeviceResource(
                    id = "device-1",
                    memberId = "member-1",
                    activationAddressID = "address-1",
                    state = AuthDeviceState.Active.value,
                    name = "Mobile Phone 1",
                    localizedClientName = "Proton Account for Web",
                    platform = "Web",
                    createTime = 1715720090,
                    activateTime = 1715720090,
                    rejectTime = 1715720090,
                    activationToken = "activation-token",
                    lastActivityTime = 1715720090
                )
            ),
            events?.first()
        )
    }
}
