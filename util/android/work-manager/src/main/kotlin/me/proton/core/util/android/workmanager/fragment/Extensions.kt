@file:Suppress("unused")

package me.proton.core.util.android.workmanager.fragment

import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import me.proton.core.util.android.workmanager.WorkRequestObserver
import me.proton.core.util.android.workmanager.observe
import me.proton.core.util.android.workmanager.observeInfo
import me.proton.core.util.kotlin.NeedSerializable

/*
 * Set of extensions for Observe a `WorkRequest` within a Fragment
 * Author: Davide Farella
 */

/**
 * Triggers [callback] with [Data] when the Worker for [WorkRequest] succeed.
 * It will never be triggered if Worker is not completed successfully
 *
 * @param fragment [Fragment] required for  [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param callback lambda with [Data] as parameter, triggered when Work succeed
 *
 * @return [WorkRequest] for operator concatenation
 */
fun WorkRequest.doOnSuccess(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    callback: (workData: Data) -> Unit
): WorkRequest {
    observe(fragment, workManager, onSuccess = callback)
    return this
}

/**
 * Triggers [callback] with [T] when the Worker for [WorkRequest] succeed.
 * It will never be triggered if Worker is not completed successfully
 *
 * @param fragment [Fragment] required for  [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param callback lambda with [Data] as parameter, triggered when Work succeed
 *
 * @return [WorkRequest] for operator concatenation
 */
@NeedSerializable
@JvmName("parametrizedDoOnSuccess")
inline fun <reified T : Any> WorkRequest.doOnSuccess(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    noinline callback: (T) -> Unit
): WorkRequest {
    observe(fragment, workManager, onSuccess = callback)
    return this
}

/**
 * Observe a [WorkRequest] within a [Fragment]
 *
 * @param fragment [Fragment] required for [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param block lambda with [WorkRequestObserver] as receiver
 */
fun WorkRequest.observe(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    block: WorkRequestObserver.() -> Unit
) {
    observe(workManager, fragment.viewLifecycleOwner, block)
}

/**
 * Observe a [WorkRequest] within a [Fragment]
 *
 * @param fragment [Fragment] required for [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param onSuccess lambda with [Data] as parameter that will be triggered when Work succeed
 * @param onFailure lambda that will be triggered when Work failed
 * @param onStateChange lambda with [WorkInfo.State] as parameter that is triggered every time the Work's State is
 *   changed
 */
fun WorkRequest.observe(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    onSuccess: (workData: Data) -> Unit = {},
    onFailure: () -> Unit = {},
    onStateChange: (state: WorkInfo.State) -> Unit = {}
) {
    observe(workManager, fragment.viewLifecycleOwner) {
        onSuccess(onSuccess)
        onFailure(onFailure)
        onStateChange(onStateChange)
    }
}

/**
 * Observe a [WorkRequest] parametrized with [T] within a [Fragment]
 *
 * @param fragment [Fragment] required for [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param onSuccess lambda with [T] as parameter that will be triggered when Work succeed
 * @param onFailure lambda that will be triggered when Work failed
 * @param onStateChange lambda with [WorkInfo.State] as parameter that is triggered every time the Work's State is
 *   changed
 */
@NeedSerializable
@JvmName("parametrizedObserve")
inline fun <reified T : Any> WorkRequest.observe(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    crossinline onSuccess: (workData: T) -> Unit = {},
    noinline onFailure: () -> Unit = {},
    noinline onStateChange: (state: WorkInfo.State) -> Unit = {}
) {
    observe(workManager, fragment.viewLifecycleOwner) {
        onSuccess(onSuccess)
        onFailure(onFailure)
        onStateChange(onStateChange)
    }
}

/**
 * Observe [WorkInfo] of a [WorkRequest] within a [Fragment]
 *
 * @param fragment [Fragment] required for [Fragment.getViewLifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [Fragment.getWorkManager]
 * @param callback lambda with [WorkInfo] as parameter
 */
fun WorkRequest.observeInfo(
    fragment: Fragment,
    workManager: WorkManager = fragment.getWorkManager(),
    callback: (WorkInfo) -> Unit
) {
    observeInfo(workManager, fragment.viewLifecycleOwner, callback)
}

/**
 * Get an instance of [WorkManager] from the receiver [Fragment]
 * @throws IllegalStateException if not currently associated with a context.
 */
fun Fragment.getWorkManager(): WorkManager =
    WorkManager.getInstance(requireContext())
