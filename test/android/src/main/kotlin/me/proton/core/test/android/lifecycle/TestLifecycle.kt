package me.proton.core.test.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * A class for test within a [LifecycleOwner]
 * @author Davide Farella
 */
class TestLifecycle : LifecycleOwner {

    /**
     * @return [Lifecycle.State]
     * @see LifecycleRegistry.getCurrentState
     */
    val currentState get() = registry.currentState

    /** A [LifecycleRegistry] for the [LifecycleOwner] */
    private val registry = LifecycleRegistry(this)


    /**
     * Call [Lifecycle.Event.ON_CREATE] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun create() = handleLifecycleEvent(ON_CREATE)

    /**
     * Call [Lifecycle.Event.ON_START] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun start() = handleLifecycleEvent(ON_START)

    /**
     * Call [Lifecycle.Event.ON_RESUME] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun resume() = handleLifecycleEvent(ON_RESUME)

    /**
     * Call [Lifecycle.Event.ON_PAUSE] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun pause() = handleLifecycleEvent(ON_PAUSE)

    /**
     * Call [Lifecycle.Event.ON_STOP] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun stop() = handleLifecycleEvent(ON_STOP)

    /**
     * Call [Lifecycle.Event.ON_DESTROY] on [Lifecycle]
     * @return this [TestLifecycle]
     */
    fun destroy() = handleLifecycleEvent(ON_DESTROY)

    /** Handle all the [events] sequentially */
    operator fun invoke(vararg events: Lifecycle.Event) {
        events.forEach { handleLifecycleEvent(it) }
    }


    /** @see LifecycleOwner.lifecycle */
    override val lifecycle: Lifecycle = registry

    /** @see LifecycleRegistry.handleLifecycleEvent */
    private fun handleLifecycleEvent(event: Lifecycle.Event) = apply {
        registry.handleLifecycleEvent(event)
    }
}
