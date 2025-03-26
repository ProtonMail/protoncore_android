package me.proton.core.devicemigration.domain.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.SessionForkSelector
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class ObserveEdmCodeTest {
    @MockK
    private lateinit var generateEdmCode: GenerateEdmCode

    private lateinit var tested: ObserveEdmCode

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObserveEdmCode(generateEdmCode)
    }

    @Test
    fun `observe single code`() = runTest {
        // GIVEN
        coEvery { generateEdmCode(any()) } returns Pair("code", SessionForkSelector("selector"))

        // WHEN
        tested(sessionId = null).test {
            // THEN
            assertEquals(
                Pair("code", SessionForkSelector("selector")),
                awaitItem()
            )
        }
    }

    @Test
    fun `observe multiple codes`() = runTest {
        // GIVEN
        coEvery { generateEdmCode(any()) } returnsMany listOf(
            Pair("code1", SessionForkSelector("selector1")),
            Pair("code2", SessionForkSelector("selector2"))
        )

        // WHEN
        tested(sessionId = null).test {
            // THEN
            assertEquals(0, testScheduler.currentTime)
            assertEquals(
                Pair("code1", SessionForkSelector("selector1")),
                awaitItem()
            )

            assertEquals(
                Pair("code2", SessionForkSelector("selector2")),
                awaitItem()
            )
            assertEquals(10.minutes.inWholeMilliseconds, testScheduler.currentTime)
        }
    }

    @Test
    fun `generating code throws error which is rethrown`() = runTest {
        // GIVEN
        coEvery { generateEdmCode(any()) } throws Exception("Cannot generate")

        // WHEN
        tested(sessionId = null).test {
            // THEN
            assertEquals("Cannot generate", awaitError().message)
        }
    }
}