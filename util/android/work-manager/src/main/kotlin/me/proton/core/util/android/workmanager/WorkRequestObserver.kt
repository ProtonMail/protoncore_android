package me.proton.core.util.android.workmanager

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkRequest
import me.proton.core.util.kotlin.NeedSerializable

/**
 * An entity that allow us to observe a [WorkRequest] in a nice concise style.
 * It will be generate by [WorkRequest.observe] extensions, and allow us to set callback like the following:
 *
 * * [onSuccess] with a [Data]
 * * [onSuccess] with a deserialized object
 * * [onFailure]
 * * [onStateChange] with a [WorkInfo.State]
 *
 * @author Davide Farella
 */
class WorkRequestObserver {
    @PublishedApi // inline
    internal var successCallback = { _: Data -> }; private set
    internal var failureCallback = {}; private set
    internal var stateCallback = { _: WorkInfo.State -> }; private set

    /** Executes [block] with [Data] when relative Work is Succeed */
    fun onSuccess(block: (workData: Data) -> Unit) {
        successCallback = block
    }

    /** Executes [block] with [Data] when relative Work is Succeed */
    @NeedSerializable
    @JvmName("parametrizedOnSuccess")
    inline fun <reified T : Any> onSuccess(crossinline block: (T) -> Unit) {
        onSuccess { block(it.deserialize()) }
    }

    /** Executes [block] when relative Work is Succeed */
    fun onFailure(block: () -> Unit) {
        failureCallback = block
    }

    /** Executes [block] when relative Work is Succeed */
    fun onStateChange(block: (state: WorkInfo.State) -> Unit) {
        stateCallback = block
    }
}
