@file:Suppress("unused")

package me.proton.core.util.android.workmanager.activity

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import me.proton.core.util.android.workmanager.WorkRequestObserver
import me.proton.core.util.android.workmanager.observe
import me.proton.core.util.android.workmanager.observeInfo
import me.proton.core.util.kotlin.NeedSerializable

/*
 * Set of extensions for Observe a `WorkRequest` within an Activity
 * Author: Davide Farella
 */

/**
 * Triggers [callback] with [Data] when the Worker for [WorkRequest] succeed.
 * It will never be triggered if Worker is not completed successfully
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param callback lambda with [Data] as parameter, triggered when Work succeed
 *
 * @return [WorkRequest] for operator concatenation
 */
fun WorkRequest.doOnSuccess(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    callback: (workData: Data) -> Unit
): WorkRequest {
    observe(activity, workManager, onSuccess = callback)
    return this
}

/**
 * Triggers [callback] with [T] when the Worker for [WorkRequest] succeed.
 * It will never be triggered if Worker is not completed successfully
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param callback lambda with [Data] as parameter, triggered when Work succeed
 *
 * @return [WorkRequest] for operator concatenation
 */
@NeedSerializable
@JvmName("parametrizedDoOnSuccess")
inline fun <reified T : Any> WorkRequest.doOnSuccess(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    noinline callback: (T) -> Unit
): WorkRequest {
    observe(activity, workManager, onSuccess = callback)
    return this
}

/**
 * Observe a [WorkRequest] within a [FragmentActivity]
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param block lambda with [WorkRequestObserver] as receiver
 */
fun WorkRequest.observe(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    block: WorkRequestObserver.() -> Unit
) {
    observe(workManager, activity, block)
}

/**
 * Observe a [WorkRequest] within a [FragmentActivity]
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param onSuccess lambda with [Data] as parameter that will be triggered when Work succeed
 * @param onFailure lambda that will be triggered when Work failed
 * @param onStateChange lambda with [WorkInfo.State] as parameter that is triggered every time the Work's State is
 *   changed
 */
fun WorkRequest.observe(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    onSuccess: (workData: Data) -> Unit = {},
    onFailure: () -> Unit = {},
    onStateChange: (state: WorkInfo.State) -> Unit = {}
) {
    observe(workManager, activity) {
        onSuccess(onSuccess)
        onFailure(onFailure)
        onStateChange(onStateChange)
    }
}

/**
 * Observe a [WorkRequest] parametrized with [T] within a [FragmentActivity]
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param onSuccess lambda with [T] as parameter that will be triggered when Work succeed
 * @param onFailure lambda that will be triggered when Work failed
 * @param onStateChange lambda with [WorkInfo.State] as parameter that is triggered every time the Work's State is
 *   changed
 */
@NeedSerializable
@JvmName("parametrizedObserve")
inline fun <reified T : Any> WorkRequest.observe(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    crossinline onSuccess: (workData: T) -> Unit = {},
    noinline onFailure: () -> Unit = {},
    noinline onStateChange: (state: WorkInfo.State) -> Unit = {}
) {
    observe(workManager, activity) {
        onSuccess(onSuccess)
        onFailure(onFailure)
        onStateChange(onStateChange)
    }
}

/**
 * Observe [WorkInfo] of a [WorkRequest] within a [FragmentActivity]
 *
 * @param activity [FragmentActivity] required for get [LifecycleOwner]
 * @param workManager [WorkManager] for observe the request.
 *   Default it [FragmentActivity.getWorkManager]
 * @param callback lambda with [WorkInfo] as parameter
 */
fun WorkRequest.observeInfo(
    activity: FragmentActivity,
    workManager: WorkManager = activity.getWorkManager(),
    callback: (WorkInfo) -> Unit
) {
    observeInfo(workManager, activity, callback)
}

/**
 * Get an instance of [WorkManager] from the receiver [FragmentActivity]
 */
fun FragmentActivity.getWorkManager(): WorkManager =
    WorkManager.getInstance(this)
