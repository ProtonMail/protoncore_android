@file:Suppress("unused")

package me.proton.core.util.android.workmanager.activity

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import me.proton.core.util.android.workmanager.WorkRequestObserver

/**
 * An interface containing `Lifecycle`-driven observe extensions functions for `Activity`
 *
 * Implements this interface on your `Activity` for be able to call [WorkRequest] extensions without
 * passing the [LifecycleOwner] explicitly.
 * I.e. `` workRequest.observe{ ... } `` it's a valid function that will observe the data respecting
 * the `Activity`s `Lifecycle`.
 *
 * Refer to `ObserverExtensions.kt` for methods documentation.
 * Functions can't be inlined since it's inside an interface and cannot be extensions since they're already extensions
 * on [WorkRequest]
 */
interface WorkManagerActivity : LifecycleOwner {

    /**
     * Retrieves an instance of [WorkManager]
     * It can be overridden by the [FragmentActivity] for provide it via DI
     */
    open val workManger: WorkManager get() = activity.getWorkManager()

    // region shadow methods
    private val activity get() = this as FragmentActivity
    // endregion

    fun WorkRequest.doOnSuccess(block: (workData: Data) -> Unit): WorkRequest {
        doOnSuccess(activity, workManger, block)
        return this
    }

    fun WorkRequest.doOnFailure(block: () -> Unit): WorkRequest {
        observe(activity, workManger, onFailure = block)
        return this
    }

    fun WorkRequest.doOnStateChange(block: (WorkInfo.State) -> Unit): WorkRequest {
        observe(activity, workManger, onStateChange = block)
        return this
    }

    fun WorkRequest.observe(block: WorkRequestObserver.() -> Unit) {
        observe(activity, workManger, block)
    }

    fun WorkRequest.observe(
        onSuccess: (workData: Data) -> Unit = {},
        onFailure: () -> Unit = {},
        onStateChange: (state: WorkInfo.State) -> Unit = {}
    ) {
        observe(activity, workManger, onSuccess, onFailure, onStateChange)
    }

    fun WorkRequest.observeInfo(block: (WorkInfo) -> Unit) {
        observeInfo(activity, workManger, block)
    }
}
