@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Execute a [block] when [LifecycleOwner] pass the [ON_DESTROY] event
 *
 * @param removeObserver if `true` call [Lifecycle.removeObserver]
 * Default is `false`
 */
inline fun LifecycleOwner.doOnDestroy(
    removeObserver: Boolean = false,
    crossinline block: LifecycleOwner.(LifecycleObserver) -> Unit
) {
    val observer = object : BaseLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            if (removeObserver) lifecycle.removeObserver(this)
            owner.block(this)
        }
    }
    lifecycle.addObserver(observer)
}

/**
 * Execute a [block] when [Fragment.getViewLifecycleOwner] pass the [ON_DESTROY] event
 *
 * @param removeObserver if `true` call [Lifecycle.removeObserver]
 * Default is `false`
 */
inline fun Fragment.doOnViewDestroy(
    removeObserver: Boolean = false,
    crossinline block: LifecycleOwner.(LifecycleObserver) -> Unit
) {
    val viewLifecycle = viewLifecycleOwner.lifecycle
    val observer = object : BaseLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            if (removeObserver) viewLifecycle.removeObserver(this)
            owner.block(this)
        }
    }
    viewLifecycle.addObserver(observer)
}

/**
 * Execute a [block] when [View] is detached.
 *
 * @param removeListener if `true` call [View.removeOnAttachStateChangeListener]
 * Default is `false`
 */
inline fun View.doOnDetach(
    removeListener: Boolean = false,
    crossinline block: View.(View.OnAttachStateChangeListener) -> Unit
) {
    val listener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(view: View) {
            if (removeListener) view.removeOnAttachStateChangeListener(this)
            view.block(this)
        }

        override fun onViewAttachedToWindow(view: View) { /* Noop */ }
    }
    addOnAttachStateChangeListener(listener)
}

/**
 * Interface with default methods for [LifecycleObserver]
 * Inherit from [LifecycleEventObserver]
 */
private interface BaseLifecycleObserver : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> onCreate(source)
            ON_START -> onStart(source)
            ON_RESUME -> onResume(source)
            ON_PAUSE -> onPause(source)
            ON_STOP -> onStop(source)
            ON_DESTROY -> onDestroy(source)
            ON_ANY -> { /* Do nothing, proper event already called */
            }
        }
    }

    fun onCreate(owner: LifecycleOwner) {}
    fun onStart(owner: LifecycleOwner) {}
    fun onResume(owner: LifecycleOwner) {}
    fun onPause(owner: LifecycleOwner) {}
    fun onStop(owner: LifecycleOwner) {}
    fun onDestroy(owner: LifecycleOwner) {}
}
