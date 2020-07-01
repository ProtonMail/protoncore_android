package me.proton.core.util.android.workmanager

import androidx.lifecycle.LifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest

@PublishedApi
internal fun WorkRequest.observe(
    workManager: WorkManager,
    lifecycleOwner: LifecycleOwner,
    block: WorkRequestObserver.() -> Unit
) {
    val observer = WorkRequestObserver().apply(block)

    var lastState: WorkInfo.State? = null
    observeInfo(workManager, lifecycleOwner) { info ->

        val state = info.state
        if (state != lastState) {
            lastState = state
            observer.stateCallback(state)
        }
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (state) {
            WorkInfo.State.SUCCEEDED -> observer.successCallback(info.outputData)
            WorkInfo.State.FAILED -> observer.failureCallback()
        }
    }
}

internal fun WorkRequest.observeInfo(
    workManager: WorkManager,
    lifecycleOwner: LifecycleOwner,
    callback: (WorkInfo) -> Unit
) {
    workManager.getWorkInfoByIdLiveData(id).observe(lifecycleOwner, callback)
}
