package ch.protonmail.libs.core.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ch.protonmail.libs.testAndroid.lifecycle.TestLifecycle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

/**
 * Test suite for [Lifecycle] Utils
 * @author Davide Farella
 */
internal class LifecycleUtilsTest {

    private val mockLambda: LifecycleOwner.(LifecycleObserver) -> Unit = mockk(relaxed = true)

    @Test
    fun `LifecycleOwner doOnDestroy without removeObserver`() {
        val lifecycle = TestLifecycle()
        lifecycle.doOnDestroy(removeObserver = false, block = mockLambda)

        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 2) { lifecycle.mockLambda(any()) }
    }

    @Test
    fun `LifecycleOwner doOnDestroy with removeObserver`() {
        val lifecycle = TestLifecycle()
        lifecycle.doOnDestroy(removeObserver = true, block = mockLambda)

        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
    }

    @Test
    fun `Fragment doOnViewDestroy without removeObserver`() {
        val lifecycle = TestLifecycle()
        val fragment = mockk<Fragment>(relaxed = true) {
            every { viewLifecycleOwner } returns lifecycle
        }
        fragment.doOnViewDestroy(removeObserver = false, block = mockLambda)

        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 2) { lifecycle.mockLambda(any()) }
    }

    @Test
    fun `Fragment doOnViewDestroy with removeObserver`() {
        val lifecycle = TestLifecycle()
        val fragment = mockk<Fragment>(relaxed = true) {
            every { viewLifecycleOwner } returns lifecycle
        }
        fragment.doOnViewDestroy(removeObserver = true, block = mockLambda)

        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
        lifecycle(ON_START, ON_DESTROY)
        verify(exactly = 1) { lifecycle.mockLambda(any()) }
    }
}
