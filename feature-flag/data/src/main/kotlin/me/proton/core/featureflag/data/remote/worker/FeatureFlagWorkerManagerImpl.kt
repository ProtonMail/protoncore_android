package me.proton.core.featureflag.data.remote.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.data.R
import me.proton.core.featureflag.domain.FeatureFlagWorkerManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public class FeatureFlagWorkerManagerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val workManager: WorkManager
) : FeatureFlagWorkerManager {

    override fun enqueueOneTime(userId: UserId?) {
        workManager.enqueueUniqueWork(
            FetchUnleashTogglesWorker.getOneTimeUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            FetchUnleashTogglesWorker.getOneTimeWorkRequest(userId)
        )
    }

    override fun enqueuePeriodic(userId: UserId?, immediately: Boolean) {
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

    override fun cancel(userId: UserId?) {
        workManager.cancelUniqueWork(FetchUnleashTogglesWorker.getPeriodicUniqueWorkName(userId))
        workManager.cancelUniqueWork(FetchUnleashTogglesWorker.getOneTimeUniqueWorkName(userId))
    }


    override fun update(featureFlag: FeatureFlag) {
        val request = UpdateFeatureFlagWorker.getRequest(
            featureFlag.userId,
            featureFlag.featureId,
            featureFlag.value
        )
        workManager.enqueue(request)
    }

    override fun prefetch(userId: UserId?, featureIds: Set<FeatureId>) {
        workManager.enqueueUniqueWork(
            FetchFeatureIdsWorker.getUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            FetchFeatureIdsWorker.getRequest(userId, featureIds),
        )
    }

    private fun getRepeatIntervalBackgroundAuth(): Duration = context.resources.getInteger(
        R.integer.core_feature_feature_flag_worker_repeat_interval_auth_seconds
    ).toDuration(DurationUnit.SECONDS)

    private fun getRepeatIntervalBackgroundUnauth(): Duration = context.resources.getInteger(
        R.integer.core_feature_feature_flag_worker_repeat_interval_unauth_seconds
    ).toDuration(DurationUnit.SECONDS)
}
