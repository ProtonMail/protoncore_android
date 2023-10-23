package me.proton.core.featureflag.data.remote.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.R
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public class FeatureFlagWorkerManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val workManager: WorkManager
) {

    public fun enqueueOneTime(userId: UserId?) {
        workManager.enqueueUniqueWork(
            FetchUnleashTogglesWorker.getOneTimeUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            FetchUnleashTogglesWorker.getOneTimeWorkRequest(userId)
        )
    }

    public fun enqueuePeriodic(userId: UserId?, immediately: Boolean) {
        val repeatInterval = when (userId) {
            null -> getRepeatIntervalBackgroundUnauth()
            else -> getRepeatIntervalBackgroundAuth()
        }
        workManager.enqueueUniquePeriodicWork(
            FetchUnleashTogglesWorker.getPeriodicUniqueWorkName(userId),
            if (immediately) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
            FetchUnleashTogglesWorker.getPeriodicWorkRequest(userId, repeatInterval),
        )
    }

    public fun cancel(userId: UserId?) {
        workManager.cancelUniqueWork(FetchUnleashTogglesWorker.getPeriodicUniqueWorkName(userId))
        workManager.cancelUniqueWork(FetchUnleashTogglesWorker.getOneTimeUniqueWorkName(userId))
    }

    private fun getRepeatIntervalBackgroundAuth(): Duration = context.resources.getInteger(
        R.integer.core_feature_feature_flag_worker_repeat_interval_auth_seconds
    ).toDuration(DurationUnit.SECONDS)

    private fun getRepeatIntervalBackgroundUnauth(): Duration = context.resources.getInteger(
        R.integer.core_feature_feature_flag_worker_repeat_interval_unauth_seconds
    ).toDuration(DurationUnit.SECONDS)
}
