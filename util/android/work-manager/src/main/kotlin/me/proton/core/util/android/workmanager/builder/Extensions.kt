package me.proton.core.util.android.workmanager.builder

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.work.WorkRequest
import androidx.work.WorkRequest.Builder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST

/**
 * Marks a [WorkRequest] as important.
 *
 * Note: Only applied to clients running Android [12 | S | 31] and above.
 */
fun <B : Builder<B, *>, W : WorkRequest> Builder<B, W>.setExpeditedIfPossible(): Builder<B, W> {
    return apply {
        if (SDK_INT >= VERSION_CODES.S) {
            setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
    }
}
