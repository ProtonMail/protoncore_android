package me.proton.core.test.android.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice

private const val LOADING_TIMEOUT_MS = 30_000L

interface WithUiDevice {
    val uiDevice: UiDevice get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun <T : Any> SearchCondition<T>.waitForIt(): T {
        val result = uiDevice.wait(this, LOADING_TIMEOUT_MS)
        check(result != null) { "Could not find a UI object." }
        return result
    }
}
