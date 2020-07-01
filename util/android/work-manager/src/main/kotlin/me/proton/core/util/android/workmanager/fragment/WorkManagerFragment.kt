@file:Suppress("unused")

package me.proton.core.util.android.workmanager.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import me.proton.core.util.android.workmanager.WorkRequestObserver

/**
 * An interface containing `Lifecycle`-driven observe extensions functions for `Fragment`
 *
 * Implements this interface on your `Fragment` for be able to call [WorkRequest] extensions without
 * passing the [LifecycleOwner] explicitly.
 * I.e. `` workRequest.observe { ... } `` it's a valid function that will observe the data respecting
 * the `Fragment`s View `Lifecycle`.
 *
 * Refer to `ObserverExtensions.kt` for methods documentation.
 * Functions can't be inlined since it's inside an interface and cannot be extensions since they're already extensions
 * on [WorkRequest]
 *
 * @author Davide Farella
 */
interface WorkManagerFragment : LifecycleOwner {

    /**
     * Retrieves an instance of [WorkManager]
     * It can be overridden by the [Fragment] for provide it via DI
     */
    open val workManger: WorkManager get() = fragment.getWorkManager()

    // region shadow methods - Some are not needed, but having these abstract methods ensure that this class will be
    //  inherited from a Fragment
    private val fragment get() = this as Fragment
    fun requireContext(): Context
    fun getViewLifecycleOwner(): LifecycleOwner
    // endregion

    fun WorkRequest.doOnSuccess(block: (workData: Data) -> Unit): WorkRequest {
        doOnSuccess(fragment, workManger, block)
        return this
    }

    fun WorkRequest.doOnFailure(block: () -> Unit): WorkRequest {
        observe(fragment, workManger, onFailure = block)
        return this
    }

    fun WorkRequest.doOnStateChange(block: (WorkInfo.State) -> Unit): WorkRequest {
        observe(fragment, workManger, onStateChange = block)
        return this
    }

    fun WorkRequest.observe(block: WorkRequestObserver.() -> Unit) {
        observe(fragment, workManger, block)
    }

    fun WorkRequest.observe(
        onSuccess: (workData: Data) -> Unit = {},
        onFailure: () -> Unit = {},
        onStateChange: (state: WorkInfo.State) -> Unit = {}
    ) {
        observe(fragment, workManger, onSuccess, onFailure, onStateChange)
    }

    fun WorkRequest.observeInfo(block: (WorkInfo) -> Unit) {
        observeInfo(fragment, workManger, block)
    }
}
