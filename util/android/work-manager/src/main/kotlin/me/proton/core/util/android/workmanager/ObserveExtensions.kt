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
            observer.stateCallback.invoke(state)
        }

        when (state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING -> Unit
            WorkInfo.State.BLOCKED,
            WorkInfo.State.CANCELLED,
            WorkInfo.State.FAILED -> observer.failureCallback.invoke()
            WorkInfo.State.SUCCEEDED -> observer.successCallback.invoke(info.outputData)
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
